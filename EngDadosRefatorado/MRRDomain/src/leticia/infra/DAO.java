package leticia.infra;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import leticia.cliente.Cliente;
import leticia.mrr.Parcela;
import leticia.mrr.Tipo;

public class DAO {

	private static EntityManagerFactory factory;
	private EntityManager manager;

	private static String persistenceUnit;

	public void begin() {
		try {
			if (factory == null) {
				factory = Persistence.createEntityManagerFactory(persistenceUnit);
			}

			manager = factory.createEntityManager();

			manager.getTransaction().begin();
		} catch (Exception e) {
			factory = null;
		}
	}

	public void persist(Object obj) {
		manager.persist(obj);
	}

	public void commit() {
		manager.getTransaction().commit();

		manager.close();

	}

	public static void setPersistenceUnit(String persistenceUnit) {
		DAO.persistenceUnit = persistenceUnit;
		factory = null;

	}

	public <T> List<T> get(Class<T> classe) {
		if (manager == null) {
			throw new RuntimeException("Faltou begin");
		}

		return manager.createQuery("select c from " + classe.getName() + " c ").getResultList();

	}

	public <T> List<T> get(Class<T> classe, String where) {
		if (manager == null) {
			throw new RuntimeException("Faltou begin");
		}

		return manager.createQuery("select c from " + classe.getName() + " c where " + where).getResultList();
	}

	public <T> List<T> get(Class<T> classe, String where, Object... param) {

		if (manager == null) {
			throw new RuntimeException("Faltou begin");
		}

		Query q = manager.createQuery("select c from " + classe.getName() + " c where " + where);

		for (int i = 0; i < param.length; i++) {
			q.setParameter(i + 1, param[i]);
		}

		return q.getResultList();

	}

	public static void resetFactory() {
		factory = null;

	}

	public <T> T find(Class<?> classe, int id) {
		if (manager == null) {
			throw new RuntimeException("Faltou begin");
		}

		return (T) manager.find(classe, id);
	}

	public void merge(Object object) {
		manager.merge(object);

	}

	public List<?> get(String query,   Object... param) {
		Query q = manager.createQuery(query);

		for (int i = 0; i < param.length; i++) {
			q.setParameter(i + 1, param[i]);
		}

		return q.getResultList();
	}

}
