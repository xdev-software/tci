package software.xdev.tci.db.persistence.classfinder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;


public class AnnotatedClassFinder
{
	@SuppressWarnings({"java:S1452", "java:S4968"}) // Returned so by stream
	public List<? extends Class<?>> find(
		final String basePackage,
		final Class<? extends Annotation> annotationClazz)
	{
		final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
		
		try
		{
			return Stream.of(resourcePatternResolver.getResources(
					ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
						+ this.resolveBasePackage(basePackage) + "/" + "**/*.class"))
				.filter(Resource::isReadable)
				.map(resource -> {
					try
					{
						return this.getIfIsCandidate(
							metadataReaderFactory.getMetadataReader(resource),
							annotationClazz);
					}
					catch(final IOException e)
					{
						return null;
					}
				})
				.filter(Objects::nonNull)
				.toList();
		}
		catch(final IOException ioe)
		{
			throw new UncheckedIOException(ioe);
		}
	}
	
	protected String resolveBasePackage(final String basePackage)
	{
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}
	
	protected Class<?> getIfIsCandidate(
		final MetadataReader metadataReader,
		final Class<? extends Annotation> annotationClazz)
	{
		try
		{
			final Class<?> clazz = Class.forName(metadataReader.getClassMetadata().getClassName());
			if(clazz.getAnnotation(annotationClazz) != null)
			{
				return clazz;
			}
		}
		catch(final Exception e)
		{
			// Nothing
		}
		return null;
	}
}
