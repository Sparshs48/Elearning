package com.example.demo.dao;

import org.springframework.stereotype.Repository;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.config.HibernateUtil;
import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;

@Repository
public class EnrollmentDAO {
    public Enrollment save(Enrollment enrollment) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(enrollment);
            tx.commit();
            return enrollment;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    public List<Enrollment> findByStudent(User student) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Enrollment> query = session.createQuery(
                "FROM Enrollment e JOIN FETCH e.course WHERE e.student = :student", 
                Enrollment.class
            );
            query.setParameter("student", student);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }

    public Long getActiveEnrollmentsCount(User student) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Long> query = session.createQuery(
                "SELECT COUNT(e) FROM Enrollment e WHERE e.student = :student AND e.status = 'ACTIVE'",
                Long.class
            );
            query.setParameter("student", student);
            return query.uniqueResult();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public Enrollment findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.get(Enrollment.class, id);
        } finally {
            if (session != null) session.close();
        }
    }
    
    public List<Enrollment> findByStudentAndCourse(User student, Long courseId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Enrollment> query = session.createQuery(
                "FROM Enrollment WHERE student = :student AND course.id = :courseId AND status = 'ACTIVE'", 
                Enrollment.class
            );
            query.setParameter("student", student);
            query.setParameter("courseId", courseId);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public void dropEnrollment(Long enrollmentId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            Enrollment enrollment = session.get(Enrollment.class, enrollmentId);
            if (enrollment != null) {
                enrollment.setStatus("DROPPED");
                session.merge(enrollment);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }
    
    public List<Enrollment> findByCourse(Course course) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Enrollment> query = session.createQuery(
                "FROM Enrollment WHERE course = :course", 
                Enrollment.class
            );
            query.setParameter("course", course);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public List<Enrollment> findByStudentAndCourse(User student, Course course) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Enrollment> query = session.createQuery(
                "FROM Enrollment WHERE student = :student AND course = :course", 
                Enrollment.class
            );
            query.setParameter("student", student);
            query.setParameter("course", course);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }

}