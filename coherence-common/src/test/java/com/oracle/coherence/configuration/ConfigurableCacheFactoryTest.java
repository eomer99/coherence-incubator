/*
 * File: ConfigurableCacheFactoryTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.coherence.configuration;

import com.oracle.tools.junit.AbstractTest;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheFactoryBuilder;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.net.cache.WrapperNamedCache;

import com.tangosol.run.xml.XmlDocument;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.run.xml.XmlValue;

import com.tangosol.util.Base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

/**
 * Functional Tests for Coherence Configurable Cache Factories.
 * <p>
 * Copyright (c) 2013. Jonathan Knight.
 *
 * @author Jonathan Knight
 */
public class ConfigurableCacheFactoryTest extends AbstractTest
{
    /**
     * The ClassLoader to be used to isolate the ConfigurableCacheFactory(s).
     */
    private ClassLoader m_loader;

    /**
     * The CacheFactoryBuilder to be used to load ConfigurableCacheFactory(s).
     */
    private CacheFactoryBuilder m_builder;


    /**
     * Setup a Test
     */
    @Before
    public void setup()
    {
        m_loader  = Base.getContextClassLoader();
        m_builder = CacheFactory.getCacheFactoryBuilder();
    }


    /**
     * Tear down a Test
     */
    @After
    public void cleanup()
    {
        m_builder.releaseAll(m_loader);
        CacheFactory.shutdown();
    }


    /**
     * Ensure we can create a custom scheme using a no-args constructor.
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassSchemeWithNoParameters() throws Exception
    {
        Properties properties = new Properties();

        properties.setProperty("scheme.name", "class-scheme-no-params");

        ConfigurableCacheFactory cacheFactory = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                               properties,
                                                                               m_loader);

        NamedCache cache = cacheFactory.ensureCache("test-cache", m_loader);

        assertThat(cache, is(instanceOf(MyCache.class)));
    }


    /**
     * Ensure we can create a custom scheme using a {cache-name} parameter
     * in the constructor.
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassSchemeWithCacheNameMacroParameter() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();

        properties.setProperty("scheme.name", "class-scheme-cache-name-macro-param");

        ConfigurableCacheFactory cacheFactory = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                               properties,
                                                                               loader);

        NamedCache cache = cacheFactory.ensureCache("test-cache", loader);

        assertThat(cache, is(instanceOf(MyCache.class)));
        assertThat(cache.getCacheName(), is("test-cache"));
    }


    /**
     * Ensure we can create a custom scheme using a {class-loader} parameter
     * in the constructor.
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassSchemeWithClassLoaderMacroParameter() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();

        properties.setProperty("scheme.name", "class-scheme-with-class-loader-params");

        ConfigurableCacheFactory cacheFactory = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                               properties,
                                                                               loader);

        MyCache cache = (MyCache) cacheFactory.ensureCache("test-cache", loader);

        assertThat(cache, is(instanceOf(MyCache.class)));
        assertThat(cache.getClassLoader(), is(sameInstance(loader)));
    }


    /**
     * Ensure we can create a custom scheme using a {cache-ref} parameter
     * in the constructor.
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassSchemeWithCacheRefMacroParameter() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();

        properties.setProperty("scheme.name", "class-scheme-cache-ref-macro-param");

        ConfigurableCacheFactory cacheFactory = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                               properties,
                                                                               loader);

        WrapperNamedCache cache = (WrapperNamedCache) cacheFactory.ensureCache("test-cache", loader);

        assertThat(cache, is(instanceOf(MyCache.class)));
        assertThat(cache.getCacheName(), is("local-test-cache"));

        NamedCache underlying = (NamedCache) cache.getMap();

        assertThat(underlying.getCacheName(), is("local-test-cache"));
    }


    /**
     * Ensure we can create a custom scheme using a {cache-name} and {cache-ref}
     * parameter in the constructor.
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassSchemeWithCacheNameAndCacheRefMacroParameter() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();

        properties.setProperty("scheme.name", "class-scheme-cache-name-and-cache-ref-macro-param");

        ConfigurableCacheFactory cacheFactory = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                               properties,
                                                                               loader);

        WrapperNamedCache cache = (WrapperNamedCache) cacheFactory.ensureCache("test-cache", loader);

        assertThat(cache, is(instanceOf(MyCache.class)));
        assertThat(cache.getCacheName(), is("test-cache"));

        NamedCache underlying = (NamedCache) cache.getMap();

        assertThat(underlying.getCacheName(), is("local-test-cache"));
    }


    /**
     * Ensure we can create a custom scheme using a {cache-ref} that contains
     * a {cache-name} parameter in the constructor.
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassSchemeWithParameterFromMapping() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();

        properties.setProperty("scheme.name", "class-scheme-cache-name-and-cache-ref-macro-param");

        ConfigurableCacheFactory cacheFactory = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                               properties,
                                                                               loader);

        WrapperNamedCache cache = (WrapperNamedCache) cacheFactory.ensureCache("test-two-cache", loader);

        assertThat(cache, is(instanceOf(MyCache.class)));
        assertThat(cache.getCacheName(), is("test-two-cache"));

        NamedCache underlying = (NamedCache) cache.getMap();

        assertThat(underlying.getCacheName(), is("local-underlying-cache"));
    }


    /**
     * Ensure we can create a custom scheme using a {scheme-ref} that contains
     * a {cache-name} parameter in the constructor.
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassSchemeWithSchemeRefParameters() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();

        properties.setProperty("scheme.name", "class-scheme-with-scheme-ref");

        ConfigurableCacheFactory cacheFactory = createConfigurableCacheFactory("test-scheme-ref-cache-config.xml",
                                                                               properties,
                                                                               loader);

        MyCache cache = (MyCache) cacheFactory.ensureCache("test-cache", loader);

        assertThat(cache, is(instanceOf(MyCache.class)));
        assertThat(cache.getMyClass(), is(instanceOf(MyClass.class)));
    }


    /**
     * Ensure that the correct {@link NamedCache} is resolved by the {@link CacheFactory}.
     *
     * @throws Exception
     */
    @Test
    public void testWrapperNamedCacheResolution() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();
        ConfigurableCacheFactory ccf = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                      properties,
                                                                      loader);

        NamedCache cache = CacheFactory.getCache("dist-test");

        cache.put("Key-1", "Value-1");

        NamedCache cqc = CacheFactory.getCache("cqc-test");

        assertEquals(cqc.getCacheName(), "cqc-test");
    }


    /**
     * Ensure that the correct {@link NamedCache} implementation is resolved by the {@link CacheFactory}
     *
     * @throws Exception
     */
    @Test
    public void testNamedCacheImplementationResolution() throws Exception
    {
        ClassLoader loader     = Base.getContextClassLoader();
        Properties  properties = new Properties();
        ConfigurableCacheFactory ccf = createConfigurableCacheFactory("test-cache-ref-cache-config.xml",
                                                                      properties,
                                                                      loader);

        NamedCache                  cache   = CacheFactory.getCache("dist-test");
        WrapperContinuousQueryCache wrapper = (WrapperContinuousQueryCache) CacheFactory.getCache("cqc-test");
        ContinuousQueryCache        cqc     = (ContinuousQueryCache) wrapper.getMap();

        assertTrue(cqc.getCache() == cache);
    }


    /**
     * Create a ConfigurableCacheFactory to use in tests.
     * @param configName - the name of the cache configuration to load
     * @param properties - the Properties to use to replace system-property values in the configuration
     * @param loader     - the CacheLoader to use to load the configuration
     * @return a ConfigurableCacheFactory to use for tests.
     */
    private ConfigurableCacheFactory createConfigurableCacheFactory(String      configName,
                                                                    Properties  properties,
                                                                    ClassLoader loader)
    {
        XmlDocument xmlConfig = XmlHelper.loadFileOrResource(configName, "config", loader);

        replaceSystemProperties(xmlConfig, properties);
        m_builder.setCacheConfiguration(loader, xmlConfig);

        return m_builder.getConfigurableCacheFactory(loader);
    }


    /**
     * Replace all the system-property override values in the
     * specified XML using the specified properties.
     *
     * @param xml        - the XML in which to replace system-property values
     * @param properties - the Properties to use for the replacements
     */
    @SuppressWarnings("unchecked")
    public static void replaceSystemProperties(XmlElement xml,
                                               Properties properties)
    {
        String   propertyAttribute = "system-property";
        XmlValue attr              = xml.getAttribute(propertyAttribute);

        if (attr != null)
        {
            xml.setAttribute(propertyAttribute, null);

            String sValue = properties.getProperty(attr.getString());

            if (sValue != null)
            {
                xml.setString(sValue);
            }
        }

        for (XmlElement childElement : (List<XmlElement>) xml.getElementList())
        {
            replaceSystemProperties(childElement, properties);
        }
    }
}
