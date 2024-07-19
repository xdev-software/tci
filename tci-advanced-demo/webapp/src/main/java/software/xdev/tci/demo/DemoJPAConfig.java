package software.xdev.tci.demo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import software.xdev.tci.demo.persistence.config.DefaultJPAConfig;
import software.xdev.tci.demo.persistence.util.DisableHibernateFormatMapper;


@Configuration
@EnableTransactionManagement
@EntityScan(DefaultJPAConfig.ENTITY_PACKAGE)
public class DemoJPAConfig extends DefaultJPAConfig implements HibernatePropertiesCustomizer
{
	protected Set<String> propertiesToPatch()
	{
		return Set.of("hibernate.boot.allow_jdbc_metadata_access");
	}
	
	// See also: https://stackoverflow.com/a/66220157
	@Override
	public void customize(final Map<String, Object> hibernateProperties)
	{
		this.propertiesToPatch()
			.stream()
			.collect(Collectors.toMap(p -> p.replace('_', '.'), Function.identity()))
			.forEach((target, replace) ->
				Optional.ofNullable(hibernateProperties.get(target))
					.ifPresent(value -> {
						hibernateProperties.remove(target);
						hibernateProperties.put(replace, value);
					}));
		
		hibernateProperties.putAll(DisableHibernateFormatMapper.properties());
	}
}
