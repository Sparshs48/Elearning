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
import com.example.demo.model.LectureNote;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;

@Repository
public class LectureNoteDAO {
    public LectureNote save(LectureNote note) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            if (note.getId() == null) {
                session.persist(note);
            } else {
                note = session.merge(note);
            }
            
            tx.commit();
            return note;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    public List<LectureNote> findByCourse(Course course) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<LectureNote> query = session.createQuery(
                "FROM LectureNote WHERE course = :course", 
                LectureNote.class
            );
            query.setParameter("course", course);
            return query.list();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public LectureNote findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.get(LectureNote.class, id);
        } finally {
            if (session != null) session.close();
        }
    }
    
    public List<LectureNote> findByCourseInOrderByUploadDateDesc(List<Course> courses) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<LectureNote> query = session.createQuery(
                "FROM LectureNote ln WHERE ln.course IN :courses " +
                "ORDER BY ln.uploadDate DESC", 
                LectureNote.class
            );
            query.setParameterList("courses", courses);
            return query.list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}