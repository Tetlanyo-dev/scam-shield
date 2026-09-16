(function () {
  'use strict';

  const conversation = document.getElementById('conversation');
  const form = document.getElementById('ussd-form');
  const input = document.getElementById('ussd-input');
  const backButton = document.getElementById('back-button');
  const restartButton = document.getElementById('restart-button');

  const screens = {
    menu: {
      prompt: 'Welcome to ScamShield.\n\n1. Verify a caller\n2. Report a scam\n3. Emergency fraud help\n4. Security advice\n\nEnter 1-4.',
      choices: ['1', '2', '3', '4']
    },
    verify: {
      prompt: 'Enter the phone number that contacted you.\n\nWe check the number against the available registry. This does not prove who is holding the phone.',
      input: 'phone'
    },
    report: {
      prompt: 'Enter the suspicious phone number to report.\n\nDo not enter a PIN, OTP, password, or account details.',
      input: 'phone'
    },
    attackType: {
      prompt: 'What did the caller ask for?\n\n1. OTP\n2. PIN\n3. Money\n4. Something else',
      choices: ['1', '2', '3', '4']
    }
  };

  let history = [];
  let screen = 'menu';
  let reportNumber = '';

  function addMessage(text, type) {
    const message = document.createElement('div');
    message.className = 'message ' + type;
    message.textContent = text;
    conversation.appendChild(message);
    conversation.scrollTop = conversation.scrollHeight;
  }

  function showScreen(nextScreen) {
    screen = nextScreen;
    history.push(nextScreen);
    addMessage(screens[nextScreen].prompt, 'system');
    input.value = '';
    input.placeholder = screens[nextScreen].input === 'phone' ? 'e.g. 7X XXX XXX' : 'Choose an option';
    input.focus();
  }

  function normalizePhone(value) {
    return value.replace(/[\s()-]/g, '');
  }

  function isPhone(value) {
    const normalized = normalizePhone(value);
    return /^(\+267)?[0-9]{7,8}$/.test(normalized);
  }

  function finishAction(text) {
    addMessage(text, 'system');
    input.value = '';
    input.placeholder = 'Choose an option';
  }

  function handleMenu(value) {
    if (value === '1') return showScreen('verify');
    if (value === '2') return showScreen('report');
    if (value === '3') {
      finishAction('If money was taken or your account is at risk:\n\n1. Contact your mobile-money provider through an official channel.\n2. Do not share another OTP or PIN.\n3. Keep the caller number and transaction details for your report.\n\nEnter 0 to return to the main menu.');
      screen = 'advice';
      return;
    }
    if (value === '4') {
      finishAction('Stay safe:\n\n- Never share your PIN or OTP with a caller.\n- End the call and use an official provider number.\n- Verify suspicious numbers before taking action.\n\nEnter 0 to return to the main menu.');
      screen = 'advice';
      return;
    }
    addMessage('Please enter 1, 2, 3, or 4.', 'error');
  }

  function handleInput(value) {
    if (screen === 'menu') return handleMenu(value);
    if (screen === 'advice') {
      if (value === '0') return restart();
      return addMessage('Enter 0 to return to the main menu.', 'error');
    }
    if (screens[screen].input === 'phone') {
      if (!isPhone(value)) return addMessage('That number format is not recognised. Enter a valid Botswana phone number.', 'error');
      reportNumber = normalizePhone(value);
      if (screen === 'verify') {
        finishAction('Demo response for ' + value + ':\n\nStatus: UNKNOWN NUMBER\nRisk: No matching registry record in this simulator.\n\nDo not share your PIN or OTP. If the call seems suspicious, return to the menu and report it.\n\nEnter 0 to return to the main menu.');
        screen = 'advice';
        return;
      }
      return showScreen('attackType');
    }
    if (screen === 'attackType') {
      const labels = { '1': 'OTP request', '2': 'PIN request', '3': 'Money request', '4': 'Other suspicious behaviour' };
      if (!labels[value]) return addMessage('Please enter 1, 2, 3, or 4.', 'error');
      finishAction('Report received in demo mode for ' + reportNumber + '.\n\nReported behaviour: ' + labels[value] + '\nReference: DEMO-' + Date.now().toString().slice(-6) + '\n\nNever share your PIN or OTP. Enter 0 to return to the main menu.');
      screen = 'advice';
    }
  }

  function restart() {
    history = [];
    screen = 'menu';
    reportNumber = '';
    conversation.innerHTML = '';
    showScreen('menu');
  }

  form.addEventListener('submit', function (event) {
    event.preventDefault();
    const value = input.value.trim();
    if (!value) return addMessage('Enter a response to continue.', 'error');
    addMessage(value, 'user');
    handleInput(value);
  });

  backButton.addEventListener('click', function () {
    if (screen === 'menu') return;
    if (history.length > 1) {
      history.pop();
      const previous = history.pop() || 'menu';
      conversation.lastElementChild.remove();
      showScreen(previous);
    } else {
      restart();
    }
  });

  restartButton.addEventListener('click', restart);
  showScreen('menu');
}());