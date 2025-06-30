package software.xdev.tci.db.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the creation and destruction of {@link EntityManager}s.
 * <p/>
 * This should only be used when a {@link EntityManager} has to be created manually, e.g. during tests.
 */
public class EntityManagerController implements AutoCloseable
{
	private static final Logger LOG = LoggerFactory.getLogger(EntityManagerController.class);
	
	private static Set<String> cachedEntityClassNames;
	
	protected final List<EntityManager> activeEms = Collections.synchronizedList(new ArrayList<>());
	protected final EntityManagerFactory emf;
	
	public EntityManagerController(final EntityManagerFactory emf)
	{
		this.emf = Objects.requireNonNull(emf);
	}
	
	/**
	 * Creates a new {@link EntityManager} with an internal {@link EntityManagerFactory}, which can be used to load and
	 * save data in the database.
	 *
	 * <p>
	 * It may be a good idea to close the EntityManager, when you're finished with it.
	 * </p>
	 * <p>
	 * All created EntityManager are automatically cleaned up once {@link #close()} is called.
	 * </p>
	 *
	 * @return EntityManager
	 */
	public EntityManager createEntityManager()
	{
		final EntityManager em = this.emf.createEntityManager();
		this.activeEms.add(em);
		
		return em;
	}
	
	@Override
	public void close()
	{
		LOG.info("Shutting down resources");
		this.activeEms.forEach(em ->
		{
			try
			{
				if(em.getTransaction() != null && em.getTransaction().isActive())
				{
					em.getTransaction().rollback();
				}
				em.close();
			}
			catch(final Exception e)
			{
				LOG.warn("Unable to close EntityManager", e);
			}
		});
		
		LOG.info("Cleared {}x EntityManagers", this.activeEms.size());
		
		this.activeEms.clear();
		
		try
		{
			this.emf.close();
			LOG.info("Released EntityManagerFactory");
		}
		catch(final Exception e)
		{
			LOG.error("Failed to release EntityManagerFactory", e);
		}
	}
}
