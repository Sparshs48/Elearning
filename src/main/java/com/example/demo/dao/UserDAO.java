package com.example.demo.dao;

import java.util.List;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.example.demo.config.HibernateUtil;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;


@Repository
public class UserDAO {
    public User save(User user) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            if (user.getId() == null) {
                session.persist(user);
            } else {
                session.merge(user);  // Use merge instead of persist for existing entities
            }
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    public User findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.get(User.class, id);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public User findByUsername(String username) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery(
                "FROM User WHERE LOWER(username) = LOWER(:username)", 
                User.class
            );
            query.setParameter("username", username);
            return query.uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }



    public void delete(Long userId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                session.remove(user);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<User> findAllUsers() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.createQuery("FROM User", User.class).list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<User> findUsersByRole(UserRole role) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery(
                "FROM User WHERE role = :role", 
                User.class
            );
            query.setParameter("role", role);
            return query.list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<User> searchUsers(String searchTerm) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery(
                "FROM User WHERE username LIKE :search OR email LIKE :search", 
                User.class
            );
            query.setParameter("search", "%" + searchTerm + "%");
            return query.list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<User> findUsersByStatus(UserStatus status) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery(
                "FROM User WHERE status = :status", 
                User.class
            );
            query.setParameter("status", status);
            return query.list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    public User findByEmail(String email) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery(
                "FROM User WHERE email = :email", 
                User.class
            );
            query.setParameter("email", email);
            return query.uniqueResult();
        } finally {
            if (session != null) session.close();
        }
    }
    
    public Page<User> findAllPaginated(int page, int size) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery("FROM User", User.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            
            Long total = session.createQuery("SELECT COUNT(u) FROM User u", Long.class).uniqueResult();
            
            List<User> users = query.list();
            return new PageImpl<>(users, PageRequest.of(page, size), total);
        } finally {
            if (session != null) session.close();
        }
    }
    
    public Page<User> searchPaginated(String searchTerm, int page, int size) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            // Get total count
            Query<Long> countQuery = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username LIKE :search OR u.email LIKE :search",
                Long.class
            );
            countQuery.setParameter("search", "%" + searchTerm + "%");
            Long total = countQuery.uniqueResult();
            
            // Get paginated results
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.username LIKE :search OR u.email LIKE :search",
                User.class
            );
            query.setParameter("search", "%" + searchTerm + "%");
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            
            List<User> users = query.list();
            return new PageImpl<>(users, PageRequest.of(page, size), total);
        } finally {
            if (session != null) session.close();
        }
    }

    public Page<User> findByRolePaginated(UserRole role, int page, int size) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            // Get total count
            Query<Long> countQuery = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.role = :role",
                Long.class
            );
            countQuery.setParameter("role", role);
            Long total = countQuery.uniqueResult();
            
            // Get paginated results
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.role = :role",
                User.class
            );
            query.setParameter("role", role);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            
            List<User> users = query.list();
            return new PageImpl<>(users, PageRequest.of(page, size), total);
        } finally {
            if (session != null) session.close();
        }
    }
}