package software.xdev.tci.db.persistence.classfinder;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class CachedEntityAnnotatedClassNameFinder implements Supplier<Set<String>>
{
	protected final Supplier<AnnotatedClassFinder> classFinderProvider = AnnotatedClassFinder::new;
	protected final String basePackage;
	protected final Class<? extends Annotation> annotationClazz;
	protected Set<String> cache;
	
	public CachedEntityAnnotatedClassNameFinder(
		final String basePackage,
		final Class<? extends Annotation> annotationClazz)
	{
		this.basePackage = basePackage;
		this.annotationClazz = annotationClazz;
	}
	
	@Override
	public Set<String> get()
	{
		if(this.cache == null)
		{
			this.cache = this.classFinderProvider.get()
				.find(this.basePackage, this.annotationClazz)
				.stream()
				.map(Class::getName)
				.collect(Collectors.toSet());
		}
		return this.cache;
	}
}
