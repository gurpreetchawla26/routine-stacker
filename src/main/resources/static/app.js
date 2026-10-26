// Routine Stacker front end. Vanilla JavaScript only. All server text is inserted with textContent.

const FREQUENCY_LABELS = { DAILY: 'Daily', WEEKLY: 'Weekly' };

const form = document.getElementById('create-form');
const habitList = document.getElementById('habit-list');
const loadingBox = document.getElementById('loading');
const emptyBox = document.getElementById('empty');
const errorBox = document.getElementById('error');
const formErrorBox = document.getElementById('form-error');
const statusBox = document.getElementById('status');

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
    });
    if (response.status === 204) {
        return null;
    }
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(body && body.message ? body.message : 'Something went wrong. Please try again.');
    }
    return body;
}

function showError(message) {
    errorBox.textContent = message;
}

function clearError() {
    errorBox.textContent = '';
}

function announce(message) {
    statusBox.textContent = message;
}

function textItem(text) {
    const item = document.createElement('li');
    item.textContent = text;
    return item;
}

async function loadHabits() {
    loadingBox.hidden = false;
    habitList.hidden = true;
    emptyBox.hidden = true;
    try {
        const habits = await api('/api/habits');
        habitList.replaceChildren(...habits.map(renderHabit));
        habitList.hidden = habits.length === 0;
        emptyBox.hidden = habits.length !== 0;
    } catch (error) {
        showError(error.message);
    } finally {
        loadingBox.hidden = true;
    }
}

function renderHabit(habit) {
    const item = document.createElement('li');
    item.className = 'habit';

    const heading = document.createElement('h3');
    heading.textContent = habit.name;

    const meta = document.createElement('p');
    meta.textContent = `After: ${habit.anchorRoutine} · ${FREQUENCY_LABELS[habit.frequency]} · Streak: ${habit.streak}`;

    const actions = document.createElement('div');
    actions.className = 'actions';
    const toggle = document.createElement('button');
    toggle.type = 'button';
    toggle.dataset.habitId = habit.id;
    if (habit.checkedInToday) {
        toggle.textContent = `Undo check-in for ${habit.name}`;
        toggle.addEventListener('click', () => runAction(toggle, `Undid check-in for ${habit.name}.`,
            () => api(`/api/habits/${habit.id}/check-ins/today`, { method: 'DELETE' })));
    } else {
        toggle.textContent = `Check in ${habit.name}`;
        toggle.addEventListener('click', () => runAction(toggle, `Checked in ${habit.name}.`,
            () => api(`/api/habits/${habit.id}/check-ins`, { method: 'POST' })));
    }
    actions.append(toggle);

    const details = document.createElement('details');
    const summary = document.createElement('summary');
    summary.textContent = `History of ${habit.name}`;
    const historyList = document.createElement('ul');
    historyList.className = 'history';
    details.append(summary, historyList);
    details.addEventListener('toggle', () => {
        if (details.open) {
            loadHistory(habit.id, historyList);
        }
    });

    item.append(heading, meta, actions, details);
    return item;
}

async function runAction(button, message, action) {
    clearError();
    button.disabled = true;
    try {
        await action();
        announce(message);
        await loadHabits();
        focusToggle(button.dataset.habitId);
    } catch (error) {
        button.disabled = false;
        showError(error.message);
    }
}

// The list is re-rendered after each action, so the clicked button is replaced. Put focus back
// on the new toggle for the same habit so keyboard users keep their place.
function focusToggle(habitId) {
    const toggles = habitList.querySelectorAll('button[data-habit-id]');
    const match = Array.from(toggles).find((toggle) => toggle.dataset.habitId === habitId);
    if (match) {
        match.focus();
    }
}

async function loadHistory(habitId, historyList) {
    historyList.replaceChildren(textItem('Loading...'));
    try {
        const dates = await api(`/api/habits/${habitId}/check-ins`);
        historyList.replaceChildren(...(dates.length
            ? dates.map(textItem)
            : [textItem('No check-ins yet.')]));
    } catch (error) {
        historyList.replaceChildren(textItem(error.message));
    }
}

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    formErrorBox.textContent = '';
    const submit = form.querySelector('button[type="submit"]');
    submit.disabled = true;
    try {
        const created = await api('/api/habits', {
            method: 'POST',
            body: JSON.stringify({
                name: document.getElementById('habit-name').value,
                anchorRoutine: document.getElementById('habit-anchor').value,
                frequency: document.getElementById('habit-frequency').value,
            }),
        });
        form.reset();
        announce(`Added ${created.name}.`);
        await loadHabits();
    } catch (error) {
        formErrorBox.textContent = error.message;
    } finally {
        submit.disabled = false;
    }
});

loadHabits();
