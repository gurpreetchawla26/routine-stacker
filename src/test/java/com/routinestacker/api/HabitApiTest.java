package com.routinestacker.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routinestacker.support.FixedClockConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * HTTP-level tests for every endpoint and status code in architecture section 5.3, plus the error shape
 * (NFR-004) and the localhost binding (D-13). Each test method gets a fresh context so the in-memory
 * store starts empty.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(FixedClockConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HabitApiTest {

    private static final String VALID_HABIT =
            "{\"name\": \"Stretch\", \"anchorRoutine\": \"After coffee\", \"frequency\": \"DAILY\"}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment environment;

    @Test
    void serverBindsToLocalhostOnly() {
        assertEquals("localhost", environment.getProperty("server.address"));
    }

    @Test
    void createHabitReturns201WithLocationAndDerivedFields() throws Exception {
        mvc.perform(post("/api/habits").contentType("application/json").content(VALID_HABIT))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Stretch"))
                .andExpect(jsonPath("$.anchorRoutine").value("After coffee"))
                .andExpect(jsonPath("$.frequency").value("DAILY"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.streak").value(0))
                .andExpect(jsonPath("$.checkedInToday").value(false));
    }

    @Test
    void blankNameIsRejectedWith400() throws Exception {
        assertValidationError("{\"name\": \"   \", \"anchorRoutine\": \"x\", \"frequency\": \"DAILY\"}",
                "name: must not be blank");
    }

    @Test
    void nameLongerThan100CharactersIsRejected() throws Exception {
        String longName = "a".repeat(101);
        assertValidationError("{\"name\": \"" + longName + "\", \"anchorRoutine\": \"x\", \"frequency\": \"DAILY\"}",
                "name: must be at most 100 characters");
    }

    @Test
    void blankAnchorRoutineIsRejected() throws Exception {
        assertValidationError("{\"name\": \"Stretch\", \"anchorRoutine\": \"\", \"frequency\": \"DAILY\"}",
                "anchorRoutine: must not be blank");
    }

    @Test
    void unknownFrequencyIsRejectedWithAllowedValues() throws Exception {
        assertValidationError("{\"name\": \"Stretch\", \"anchorRoutine\": \"x\", \"frequency\": \"MONTHLY\"}",
                "frequency: must be one of DAILY, WEEKLY");
    }

    @Test
    void duplicateNameIsRejectedWith409RegardlessOfCaseAndSpacing() throws Exception {
        mvc.perform(post("/api/habits").contentType("application/json").content(VALID_HABIT))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/habits").contentType("application/json")
                        .content("{\"name\": \"  stretch  \", \"anchorRoutine\": \"x\", \"frequency\": \"DAILY\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        mvc.perform(get("/api/habits"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void malformedJsonIsRejectedWith400() throws Exception {
        mvc.perform(post("/api/habits").contentType("application/json").content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request body is missing or malformed"));
    }

    @Test
    void listIsEmptyWhenNoHabitsExist() throws Exception {
        mvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUnknownHabitReturns404WithErrorShape() throws Exception {
        mvc.perform(get("/api/habits/unknown-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/habits/unknown-id"));
    }

    @Test
    void checkInIsCreatedOnceAndThenReturns200() throws Exception {
        String id = createHabit();
        mvc.perform(post("/api/habits/" + id + "/check-ins"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-10-26"))
                .andExpect(jsonPath("$.created").value(true));
        mvc.perform(post("/api/habits/" + id + "/check-ins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(false));
        mvc.perform(get("/api/habits/" + id + "/check-ins"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void checkInUpdatesStreakAndCheckedInToday() throws Exception {
        String id = createHabit();
        mvc.perform(post("/api/habits/" + id + "/check-ins")).andExpect(status().isCreated());
        mvc.perform(get("/api/habits/" + id))
                .andExpect(jsonPath("$.streak").value(1))
                .andExpect(jsonPath("$.checkedInToday").value(true));
    }

    @Test
    void checkInUnknownHabitReturns404() throws Exception {
        mvc.perform(post("/api/habits/unknown-id/check-ins"))
                .andExpect(status().isNotFound());
    }

    @Test
    void undoTodayReturns204AndRemovesCheckIn() throws Exception {
        String id = createHabit();
        mvc.perform(post("/api/habits/" + id + "/check-ins")).andExpect(status().isCreated());
        mvc.perform(delete("/api/habits/" + id + "/check-ins/today")).andExpect(status().isNoContent());
        mvc.perform(get("/api/habits/" + id))
                .andExpect(jsonPath("$.checkedInToday").value(false));
    }

    @Test
    void undoWithNoCheckInTodayReturns204() throws Exception {
        String id = createHabit();
        mvc.perform(delete("/api/habits/" + id + "/check-ins/today")).andExpect(status().isNoContent());
    }

    @Test
    void undoUnknownHabitReturns404() throws Exception {
        mvc.perform(delete("/api/habits/unknown-id/check-ins/today"))
                .andExpect(status().isNotFound());
    }

    @Test
    void historyOfUnknownHabitReturns404() throws Exception {
        mvc.perform(get("/api/habits/unknown-id/check-ins"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsHabitsInCreationOrder() throws Exception {
        for (String name : List.of("First", "Second", "Third")) {
            mvc.perform(post("/api/habits").contentType("application/json")
                            .content("{\"name\": \"" + name + "\", \"anchorRoutine\": \"x\", \"frequency\": \"DAILY\"}"))
                    .andExpect(status().isCreated());
        }
        mvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("First"))
                .andExpect(jsonPath("$[1].name").value("Second"))
                .andExpect(jsonPath("$[2].name").value("Third"));
    }

    @Test
    void unsupportedMethodReturns405WithErrorShapeAndAllowHeader() throws Exception {
        mvc.perform(delete("/api/habits"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/habits"));
    }

    @Test
    void unsupportedMediaTypeReturns415WithErrorShape() throws Exception {
        mvc.perform(post("/api/habits").contentType("text/plain").content(VALID_HABIT))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/habits"));
    }

    @Test
    void unknownRouteReturns404WithErrorShape() throws Exception {
        mvc.perform(get("/api/no-such-route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/no-such-route"));
    }

    private void assertValidationError(String body, String expectedMessage) throws Exception {
        mvc.perform(post("/api/habits").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value("/api/habits"));
    }

    private String createHabit() throws Exception {
        MvcResult result = mvc.perform(post("/api/habits").contentType("application/json").content(VALID_HABIT))
                .andExpect(status().isCreated())
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
