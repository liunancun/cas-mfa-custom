package demo;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

@Configuration
public class CustomConfig {

	@Autowired
	private CasConfigurationProperties casProperties;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	@Qualifier("loginFlowRegistry")
	private FlowDefinitionRegistry loginFlowDefinitionRegistry;

	@Autowired
	private FlowBuilderServices flowBuilderServices;

	@Autowired
	@Qualifier("servicesManager")
	private ServicesManager servicesManager;

	@Bean
	public MultifactorAuthenticationProvider customAuthenticationProvider() {
		CustomMultifactorAuthenticationProvider p = new CustomMultifactorAuthenticationProvider();
		p.setBypassEvaluator(MultifactorAuthenticationUtils
				.newMultifactorAuthenticationProviderBypass(new MultifactorAuthenticationProviderBypassProperties()));
		p.setId("mfa-custom");
		return p;
	}

	@Bean
	public FlowDefinitionRegistry customFlowRegistry() {
		final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext,
				this.flowBuilderServices);
		builder.setBasePath("classpath*:/webflow");
		builder.addFlowLocationPattern("/mfa-custom/*-webflow.xml");
		return builder.build();
	}

	@Bean
	public CasWebflowConfigurer customWebflowConfigurer() {
		CasWebflowConfigurer c = new CustomMultifactorWebflowConfigurer(flowBuilderServices,
				loginFlowDefinitionRegistry, customFlowRegistry(), applicationContext, casProperties);
		c.initialize();
		return c;
	}

	@Bean
	public AuthenticationHandler customAuthenticatorAuthenticationHandler() {
		return new CustomAuthenticatorAuthenticationHandler("mfa-custom", servicesManager,
				new DefaultPrincipalFactory());
	}

	@Bean
	public AuthenticationEventExecutionPlanConfigurer esupotpAuthenticationEventExecutionPlanConfigurer() {
		return plan -> {
			plan.registerAuthenticationHandler(customAuthenticatorAuthenticationHandler());
		};
	}
}
