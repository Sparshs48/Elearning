document.getElementById('registerForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const xhr = new XMLHttpRequest();
    const registerData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        role: 'STUDENT',
        status: 'ACTIVE'
    };

    // Client-side validation
    if (!validateForm(registerData)) {
        return;
    }

    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.OPENED) {
            document.querySelector('button[type="submit"]').disabled = true;
        } else if (xhr.readyState === XMLHttpRequest.DONE) {
            document.querySelector('button[type="submit"]').disabled = false;
            
            if (xhr.status === 200) {
                window.location.href = '/login';
            } else {
                const error = xhr.responseText ? JSON.parse(xhr.responseText).message : 'Registration failed';
                alert('Registration failed: ' + error);
            }
        }
    };

    xhr.open('POST', '/api/register', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(JSON.stringify(registerData));
});

function validateForm(data) {
    if (data.username.length < 3) {
        alert('Username must be at least 3 characters long');
        return false;
    }
    
    if (!data.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
        alert('Please enter a valid email address');
        return false;
    }
    
    if (data.password.length < 6) {
        alert('Password must be at least 6 characters long');
        return false;
    }
    
    return true;
}