/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.plugin.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public abstract class LookupCache extends AbstractIdleService {
    private static final Logger LOG = LoggerFactory.getLogger(LookupCache.class);
    private String id;

    private final String name;
    private final LookupCacheConfiguration config;

    private AtomicReference<Throwable> error = new AtomicReference<>();

    protected LookupCache(String id,
                          String name,
                          LookupCacheConfiguration config) {
        this.id = id;
        this.name = name;
        this.config = config;
    }

    @Override
    protected void startUp() throws Exception {
        doStart();
    }

    protected abstract void doStart() throws Exception;

    @Override
    protected void shutDown() throws Exception {
        doStop();
    }

    protected abstract void doStop() throws Exception;

    protected void clearError() {
        error.set(null);
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(error.get());
    }

    protected void setError(Throwable throwable) {
        error.set(throwable);
    }

    @Nullable
    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract LookupResult get(LookupCacheKey key, Callable<LookupResult> loader);

    public abstract LookupResult getIfPresent(LookupCacheKey key);

    public abstract void purge();

    public abstract void purge(LookupCacheKey purgeKey);

    public LookupCacheConfiguration getConfig() {
        return config;
    }

    public String name() {
        return name;
    }

    public interface Factory<T extends LookupCache> {
        T create(@Assisted("id") String id, @Assisted("name") String name, LookupCacheConfiguration configuration);

        Descriptor getDescriptor();
    }

    public abstract static class Descriptor<C extends LookupCacheConfiguration> {

        private final String type;
        private final Class<C> configClass;

        public Descriptor(String type, Class<C> configClass) {
            this.type = type;
            this.configClass = configClass;
        }

        @JsonProperty("type")
        public String getType() {
            return type;
        }

        @JsonProperty("config_class")
        public Class<C> getConfigClass() {
            return configClass;
        }

        @JsonProperty("default_config")
        public abstract C defaultConfiguration();

    }

}
