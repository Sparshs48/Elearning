package com.example.demo.model;


public class StudentProgress {
    
    private Long id;
    
   
    private User student;
    
    private Course course;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getStudent() {
		return student;
	}

	public void setStudent(User student) {
		this.student = student;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Integer getAssignmentsCompleted() {
		return assignmentsCompleted;
	}

	public void setAssignmentsCompleted(Integer assignmentsCompleted) {
		this.assignmentsCompleted = assignmentsCompleted;
	}

	public Integer getTotalAssignments() {
		return totalAssignments;
	}

	public void setTotalAssignments(Integer totalAssignments) {
		this.totalAssignments = totalAssignments;
	}

	public Double getAverageGrade() {
		return averageGrade;
	}

	public void setAverageGrade(Double averageGrade) {
		this.averageGrade = averageGrade;
	}


    private Integer assignmentsCompleted = 0;
    
   
    private Integer totalAssignments = 0;
    
    
    private Double averageGrade = 0.0;

    // Getters and Setters
}