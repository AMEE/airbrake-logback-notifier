// Modified or written by Luca Marrocco for inclusion with hoptoad.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package code.lucamarrocco.hoptoad;

import static code.lucamarrocco.hoptoad.Exceptions.*;
import static code.lucamarrocco.hoptoad.ApiKeys.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.logging.*;
import org.junit.*;

public class HoptoadNoticeTest {
	protected static final Backtrace BACKTRACE = new Backtrace(asList("backtrace is empty"));;
	protected static final Map<String, Object> REQUEST = new HashMap<String, Object>();
	protected static final Map<String, Object> ENVIRONMENT = new HashMap<String, Object>();

	private final Log logger = LogFactory.getLog(getClass());

	private final Map<String, Object> EC2 = new HashMap<String, Object>();

	@Before
	public void setUp() {
		ENVIRONMENT.put("A_KEY", "test");
		EC2.put("AWS_SECRET", "AWS_SECRET");
		EC2.put("EC2_PRIVATE_KEY", "EC2_PRIVATE_KEY");
		EC2.put("AWS_ACCESS", "AWS_ACCESS");
		EC2.put("EC2_CERT", "EC2_CERT");
	}

	@Test
	public void testLogErrorWithException() {
		logger.error("error", newException(ERROR_MESSAGE));
	}

	@Test
	public void testLogErrorWithoutException() {
		logger.error("error");
	}

	@Test
	public void testLogThresholdLesserThatErrorWithExceptionDoNotNotifyToHoptoad() {
		logger.info("info", newException(ERROR_MESSAGE));
		logger.warn("warn", newException(ERROR_MESSAGE));
	}

	@Test
	public void testLogThresholdLesserThatErrorWithoutExceptionDoNotNotifyToHoptoad() {
		logger.info("info");
		logger.warn("warn");
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeFromException() {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, EXCEPTION).newNotice();

		assertThat(notice, is(notNullValue()));

		assertThat(notice.apiKey(), is(TEST_API_KEY));
		assertThat(notice.errorMessage(), is(ERROR_MESSAGE));
		assertThat(notice.backtrace(), is(notNullValue()));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithBacktrace() {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {
			{
				backtrace(BACKTRACE);
			}
		}.newNotice();

		assertThat(notice, is(notNullValue()));

		assertThat(notice.apiKey(), is(TEST_API_KEY));
		assertThat(notice.errorMessage(), is(ERROR_MESSAGE));
		assertThat(notice.backtrace(), is(BACKTRACE));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithEc2FilteredEnvironmentWithSystemProperties() {

		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {

			{
				environment(EC2);
				ec2EnvironmentFilters();
			}
		}.newNotice();

		final Set<String> environmentKeys = notice.environment().keySet();

		assertThat(environmentKeys, not(hasItem("AWS_SECRET")));
		assertThat(environmentKeys, not(hasItem("EC2_PRIVATE_KEY")));
		assertThat(environmentKeys, not(hasItem("AWS_ACCESS")));
		assertThat(environmentKeys, not(hasItem("EC2_CERT")));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithEnvironment() {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {
			{
				environment(ENVIRONMENT);
			}
		}.newNotice();

		assertThat(notice, is(notNullValue()));

		assertThat(notice.apiKey(), is(TEST_API_KEY));
		assertThat(notice.errorMessage(), is(ERROR_MESSAGE));
		assertThat(notice.environment().keySet(), hasItem("A_KEY"));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithErrorMessage() {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {
			{}
		}.newNotice();

		assertThat(notice, is(notNullValue()));

		assertThat(notice.apiKey(), is(TEST_API_KEY));
		assertThat(notice.errorMessage(), is(ERROR_MESSAGE));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithFilteredEnvironment() {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {
			{
				environmentFilter("A_KEY");
			}
		}.newNotice();

		final Set<String> environmentKeys = notice.environment().keySet();

		assertThat(environmentKeys, not(hasItem("A_KEY")));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithRequest() {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {
			{
				request(REQUEST);
			}
		}.newNotice();

		assertThat(notice, is(notNullValue()));

		assertThat(notice.apiKey(), is(TEST_API_KEY));
		assertThat(notice.errorMessage(), is(ERROR_MESSAGE));
		assertThat(notice.request(), is(REQUEST));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithSession() {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {
			{
				setRequest("http://localhost:3000/", "controller");
				addSessionKey("key", "value");
			}
		}.newNotice();

		assertThat(notice, is(notNullValue()));

		assertThat(notice.apiKey(), is(TEST_API_KEY));
		assertThat(notice.errorMessage(), is(ERROR_MESSAGE));
		assertThat((String) notice.session().get("key"), is("value"));

		assertTrue(notice.hasRequest());
		assertThat(notice.url(), is("http://localhost:3000/"));
		assertThat(notice.component(), is("controller"));
	}

	@Test
	public void testNewHoptoadUsingBuilderNoticeWithStandardFilteredEnvironmentWithSystemProperties() {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TEST_API_KEY, ERROR_MESSAGE) {
			{
				environment(System.getProperties());
				standardEnvironmentFilters();
			}
		}.newNotice();

		final Set<String> environmentKeys = notice.environment().keySet();

		assertThat(environmentKeys, not(hasItem("java.awt.graphicsenv")));
		assertThat(environmentKeys, not(hasItem("java.vendor.url")));
		assertThat(environmentKeys, not(hasItem("java.class.path")));
		assertThat(environmentKeys, not(hasItem("java.vm.specification")));
	}

}
