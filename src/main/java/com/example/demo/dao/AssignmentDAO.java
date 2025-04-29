package com.example.demo.dao;

import org.springframework.stereotype.Repository;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.config.HibernateUtil;
import com.example.demo.model.Assignment;
import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;import org.springframework.stereotype.Repository;


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
public class AssignmentDAO {
    public Assignment save(Assignment assignment) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            if (assignment.getId() == null) {
                session.persist(assignment);
            } else {
                assignment = session.merge(assignment);
            }
            
            tx.commit();
            return assignment;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    public List<Assignment> findByCourse(Course course) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Assignment> query = session.createQuery(
                "FROM Assignment WHERE course = :course", 
                Assignment.class
            );
            query.setParameter("course", course);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }

    public Assignment findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.get(Assignment.class, id);
        } finally {
            if (session != null) session.close();
        }
    }
}