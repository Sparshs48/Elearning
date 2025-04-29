package com.example.demo.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.config.HibernateUtil;
import com.example.demo.model.Assignment;
import com.example.demo.model.Submission;
import com.example.demo.model.User;

@Repository
public class SubmissionDAO {
    public Submission save(Submission submission) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            if (submission.getId() == null) {
                session.persist(submission);
            } else {
                submission = session.merge(submission);
            }
            
            tx.commit();
            return submission;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    public List<Submission> findByStudent(User student) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Submission> query = session.createQuery(
                "FROM Submission WHERE student = :student", 
                Submission.class
            );
            query.setParameter("student", student);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }

    public List<Submission> findByAssignment(Assignment assignment) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Submission> query = session.createQuery(
                "FROM Submission WHERE assignment = :assignment", 
                Submission.class
            );
            query.setParameter("assignment", assignment);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public List<Submission> findByAssignmentId(Long assignmentId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Submission> query = session.createQuery(
                "FROM Submission WHERE assignment.id = :assignmentId", 
                Submission.class
            );
            query.setParameter("assignmentId", assignmentId);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }

    public Submission updateGrade(Long submissionId, Double grade, String feedback) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            Submission submission = session.get(Submission.class, submissionId);
            if (submission != null) {
                submission.setGrade(grade);
                submission.setFeedback(feedback);
                submission.setStatus("GRADED");
                session.merge(submission);
            }
            
            tx.commit();
            return submission;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }
    
    public Submission findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.get(Submission.class, id);
        } finally {
            if (session != null) session.close();
        }
    }
    
    public Submission findByAssignmentAndStudent(Assignment assignment, User student) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Submission> query = session.createQuery(
                "FROM Submission WHERE assignment = :assignment AND student = :student", 
                Submission.class
            );
            query.setParameter("assignment", assignment);
            query.setParameter("student", student);
            return query.uniqueResult();
        } finally {
            if (session != null) session.close();
        }
    }

    public List<Submission> findByAssignmentAndStudent(Long assignmentId, Long studentId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Submission> query = session.createQuery(
                "FROM Submission WHERE assignment.id = :assignmentId AND student.id = :studentId", 
                Submission.class
            );
            query.setParameter("assignmentId", assignmentId);
            query.setParameter("studentId", studentId);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }

}