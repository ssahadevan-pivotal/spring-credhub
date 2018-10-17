/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.credhub.core;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.credhub.support.CredentialDetails;
import org.springframework.credhub.support.CredentialDetailsData;
import org.springframework.credhub.support.CredentialName;
import org.springframework.credhub.support.CredentialPath;
import org.springframework.credhub.support.CredentialPathData;
import org.springframework.credhub.support.CredentialRequest;
import org.springframework.credhub.support.CredentialSummary;
import org.springframework.credhub.support.CredentialSummaryData;
import org.springframework.credhub.support.ParametersRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * Implements the interactions with CredHub to save, retrieve,
 * and delete credentials.
 *
 * @author Scott Frederick 
 */
public class CredHubCredentialsTemplate implements CredHubCredentialsOperations {
	static final String BASE_URL_PATH = "/api/v1/data";
	static final String ID_URL_PATH = BASE_URL_PATH + "/{id}";
	static final String NAME_URL_QUERY = BASE_URL_PATH + "?name={name}";
	static final String NAME_URL_QUERY_CURRENT = NAME_URL_QUERY + "&current=true";
	static final String NAME_URL_QUERY_VERSIONS = NAME_URL_QUERY + "&versions={versions}";
	static final String NAME_LIKE_URL_QUERY = BASE_URL_PATH + "?name-like={name}";
	static final String PATH_URL_QUERY = BASE_URL_PATH + "?path={path}";
	static final String SHOW_ALL_URL_QUERY = BASE_URL_PATH + "?paths=true";
	static final String REGENERATE_URL_PATH = "/api/v1/regenerate";

	private CredHubOperations credHubOperations;

	/**
	 * Create a new {@link CredHubCredentialsTemplate}.
	 *
	 * @param credHubOperations the {@link CredHubOperations} to use for interactions with CredHub
	 */
	CredHubCredentialsTemplate(CredHubOperations credHubOperations) {
		this.credHubOperations = credHubOperations;
	}

	@Override
	public <T> CredentialDetails<T> write(final CredentialRequest<T> credentialRequest) {
		Assert.notNull(credentialRequest, "credentialRequest must not be null");

		final ParameterizedTypeReference<CredentialDetails<T>> ref =
				new ParameterizedTypeReference<CredentialDetails<T>>() {};

		return credHubOperations.doWithRest(new RestOperationsCallback<CredentialDetails<T>>() {
			@Override
			public CredentialDetails<T> doWithRestOperations(RestOperations restOperations) {
				ResponseEntity<CredentialDetails<T>> response =
						restOperations.exchange(BASE_URL_PATH, PUT,
								new HttpEntity<>(credentialRequest), ref);

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody();
			}
		});
	}

	@Override
	public <T, P> CredentialDetails<T> generate(final ParametersRequest<P> parametersRequest) {
		Assert.notNull(parametersRequest, "parametersRequest must not be null");

		final ParameterizedTypeReference<CredentialDetails<T>> ref =
				new ParameterizedTypeReference<CredentialDetails<T>>() {};

		return credHubOperations.doWithRest(new RestOperationsCallback<CredentialDetails<T>>() {
			@Override
			public CredentialDetails<T> doWithRestOperations(RestOperations restOperations) {
				ResponseEntity<CredentialDetails<T>> response =
						restOperations.exchange(BASE_URL_PATH, POST,
								new HttpEntity<>(parametersRequest), ref);

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody();
			}
		});
	}

	@Override
	public <T> CredentialDetails<T> regenerate(final CredentialName name) {
		Assert.notNull(name, "credential name must not be null");

		final ParameterizedTypeReference<CredentialDetails<T>> ref =
				new ParameterizedTypeReference<CredentialDetails<T>>() {};

		return credHubOperations.doWithRest(new RestOperationsCallback<CredentialDetails<T>>() {
			@Override
			public CredentialDetails<T> doWithRestOperations(RestOperations restOperations) {
				Map<String, Object> request = new HashMap<>(1);
				request.put("name", name.getName());

				ResponseEntity<CredentialDetails<T>> response =
						restOperations.exchange(REGENERATE_URL_PATH, POST,
								new HttpEntity<>(request), ref);

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody();
			}
		});
	}

	@Override
	public <T> CredentialDetails<T> getById(final String id, final Class<T> credentialType) {
		Assert.notNull(id, "credential id must not be null");
		Assert.notNull(credentialType, "credential type must not be null");

		final ParameterizedTypeReference<CredentialDetails<T>> ref =
				new ParameterizedTypeReference<CredentialDetails<T>>() {};

		return credHubOperations.doWithRest(new RestOperationsCallback<CredentialDetails<T>>() {
			@Override
			public CredentialDetails<T> doWithRestOperations(RestOperations restOperations) {
				ResponseEntity<CredentialDetails<T>> response =
						restOperations.exchange(ID_URL_PATH, GET, null, ref, id);

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody();
			}
		});
	}

	@Override
	public <T> CredentialDetails<T> getByName(final CredentialName name, final Class<T> credentialType) {
		Assert.notNull(name, "credential name must not be null");
		Assert.notNull(credentialType, "credential type must not be null");

		final ParameterizedTypeReference<CredentialDetailsData<T>> ref =
				new ParameterizedTypeReference<CredentialDetailsData<T>>() {};

		return credHubOperations.doWithRest(new RestOperationsCallback<CredentialDetails<T>>() {
			@Override
			public CredentialDetails<T> doWithRestOperations(RestOperations restOperations) {
				ResponseEntity<CredentialDetailsData<T>> response =
						restOperations.exchange(NAME_URL_QUERY_CURRENT, GET, null, ref, name.getName());

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody().getData().get(0);
			}
		});
	}

	@Override
	public <T> List<CredentialDetails<T>> getByNameWithHistory(final CredentialName name, final Class<T> credentialType) {
		Assert.notNull(name, "credential name must not be null");
		Assert.notNull(credentialType, "credential type must not be null");

		final ParameterizedTypeReference<CredentialDetailsData<T>> ref =
				new ParameterizedTypeReference<CredentialDetailsData<T>>() {};

		return credHubOperations.doWithRest(new RestOperationsCallback<List<CredentialDetails<T>>>() {
			@Override
			public List<CredentialDetails<T>> doWithRestOperations(RestOperations restOperations) {
				ResponseEntity<CredentialDetailsData<T>> response =
						restOperations.exchange(NAME_URL_QUERY, GET, null, ref, name.getName());

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody().getData();
			}
		});
	}

	@Override
	public <T> List<CredentialDetails<T>> getByNameWithHistory(final CredentialName name, final int versions,
															   final Class<T> credentialType) {
		Assert.notNull(name, "credential name must not be null");
		Assert.notNull(credentialType, "credential type must not be null");

		final ParameterizedTypeReference<CredentialDetailsData<T>> ref =
				new ParameterizedTypeReference<CredentialDetailsData<T>>() {};

		return credHubOperations.doWithRest(new RestOperationsCallback<List<CredentialDetails<T>>>() {
			@Override
			public List<CredentialDetails<T>> doWithRestOperations(RestOperations restOperations) {
				ResponseEntity<CredentialDetailsData<T>> response =
						restOperations.exchange(NAME_URL_QUERY_VERSIONS, GET, null, ref,
								name.getName(), versions);

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody().getData();
			}
		});
	}

	@Override
	public List<CredentialSummary> findByName(final CredentialName name) {
		Assert.notNull(name, "credential name must not be null");

		return credHubOperations.doWithRest(new RestOperationsCallback<List<CredentialSummary>>() {
			@Override
			public List<CredentialSummary> doWithRestOperations(
					RestOperations restOperations) {
				ResponseEntity<CredentialSummaryData> response = restOperations
						.getForEntity(NAME_LIKE_URL_QUERY,
								CredentialSummaryData.class, name.getName());

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody().getCredentials();
			}
		});
	}

	@Override
	public List<CredentialSummary> findByPath(final String path) {
		Assert.notNull(path, "credential path must not be null");

		return credHubOperations.doWithRest(new RestOperationsCallback<List<CredentialSummary>>() {
			@Override
			public List<CredentialSummary> doWithRestOperations(
					RestOperations restOperations) {
				ResponseEntity<CredentialSummaryData> response = restOperations
						.getForEntity(PATH_URL_QUERY, CredentialSummaryData.class,
								path);

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody().getCredentials();
			}
		});
	}

	@Override
	public List<CredentialPath> getAllPaths() {
		return credHubOperations.doWithRest(new RestOperationsCallback<List<CredentialPath>>() {
			@Override
			public List<CredentialPath> doWithRestOperations(
					RestOperations restOperations) {
				ResponseEntity<CredentialPathData> response = restOperations
						.getForEntity(SHOW_ALL_URL_QUERY, CredentialPathData.class);

				ExceptionUtils.throwExceptionOnError(response);

				return response.getBody().getPaths();
			}
		});
	}

	@Override
	public void deleteByName(final CredentialName name) {
		Assert.notNull(name, "credential name must not be null");

		final String name1 = name.getName();
		Assert.notNull(name1, "credential name must not be null");

		credHubOperations.doWithRest(new RestOperationsCallback<Void>() {
			@Override
			public Void doWithRestOperations(RestOperations restOperations) {
				restOperations.delete(NAME_URL_QUERY, name1);
				return null;
			}
		});
	}
}