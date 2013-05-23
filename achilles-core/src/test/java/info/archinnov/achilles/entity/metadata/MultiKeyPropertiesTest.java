package info.archinnov.achilles.entity.metadata;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * MultiKeyPropertiesTest
 * 
 * @author DuyHai DOAN
 * 
 */
public class MultiKeyPropertiesTest
{
	@Test
	public void should_to_string() throws Exception
	{
		List<Class<?>> componentClasses = Arrays.<Class<?>> asList(Integer.class, String.class);
		MultiKeyProperties props = new MultiKeyProperties();
		props.setComponentClasses(componentClasses);

		StringBuilder toString = new StringBuilder();
		toString.append("MultiKeyProperties [componentClasses=[");
		toString.append("java.lang.Integer,java.lang.String]]");

		assertThat(props.toString()).isEqualTo(toString.toString());
	}
}