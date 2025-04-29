package com.example.demo.dao;

import java.util.List;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.demo.config.HibernateUtil;
import com.example.demo.controller.UserController;
import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;

@Repository
public class CourseDAO {
	
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	  public Course save(Course course) {
		    Session session = null;
		    Transaction tx = null;
		    try {
		        session = HibernateUtil.getSessionFactory().openSession();
		        tx = session.beginTransaction();
		        
		        if (course.getId() == null) {
		            session.persist(course);
		        } else {
		            session.merge(course);
		            session.flush();
		            session.refresh(course);
		        }
		        
		        tx.commit();
		        return course;
		    } catch (Exception e) {
		        if (tx != null) tx.rollback();
		        throw e;
		    } finally {
		        if (session != null) session.close();
		    }
		}
	   public List<Course> findAll() {
		    Session session = null;
		    try {
		        session = HibernateUtil.getSessionFactory().openSession();
		        Query<Course> query = session.createQuery("FROM Course", Course.class);
		        List<Course> courses = query.list();
		        logger.info("Found {} courses in total", courses.size());
		        return courses;
		    } catch (Exception e) {
		        logger.error("Error finding all courses: ", e);
		        throw e;
		    } finally {
		        if (session != null) session.close();
		    }
		}
    public Course findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.get(Course.class, id);
        } finally {
            if (session != null) session.close();
        }
    }

    public List<Course> findByProfessor(User professor) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Course> query = session.createQuery(
                "FROM Course WHERE professor = :professor", 
                Course.class
            );
            query.setParameter("professor", professor);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public List<User> findEnrolledStudents(Course course) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery(
                "SELECT e.student FROM Enrollment e WHERE e.course = :course AND e.status = 'ACTIVE'", 
                User.class
            );
            query.setParameter("course", course);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public List<User> getEnrolledStudents(Course course) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery(
                "SELECT e.student FROM Enrollment e WHERE e.course = :course AND e.status = 'ACTIVE'",
                User.class
            );
            query.setParameter("course", course);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    public List<Course> findByEnrolledStudents(User student) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Course> query = session.createQuery(
                "SELECT e.course FROM Enrollment e " +
                "WHERE e.student = :student AND e.status = 'ACTIVE'", 
                Course.class
            );
            query.setParameter("student", student);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    

}