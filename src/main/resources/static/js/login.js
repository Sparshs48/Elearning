function showCreateProfessorForm() {
    document.getElementById('createProfessorForm').style.display = 'block';
}

document.getElementById('professorForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const professorData = {
        username: document.getElementById('username').value,
        password: document.getElementById('password').value,
        email: document.getElementById('email').value,
        role: 'PROFESSOR',
        status: 'ACTIVE'
    };

    fetch('/api/register/professor', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(professorData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to create professor');
        }
        return response.json();
    })
    .then(data => {
        alert('Professor created successfully!');
        document.getElementById('professorForm').reset();
        document.getElementById('createProfessorForm').style.display = 'none';
    })
    .catch(error => {
        alert('Error creating professor: ' + error.message);
    });
});