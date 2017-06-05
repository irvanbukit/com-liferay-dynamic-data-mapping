AUI.add(
	'liferay-ddm-form-renderer-expressions-evaluator',
	function(A) {
		var ExpressionsEvaluator = A.Component.create(
			{
				ATTRS: {
					enabled: {
						getter: '_getEnabled',
						value: true
					},

					evaluatorURL: {
						valueFn: '_valueEvaluatorURL'
					},

					form: {
					}
				},

				NAME: 'liferay-ddm-form-renderer-expressions-evaluator',

				prototype: {
					initializer: function() {
						var instance = this;

						instance._queue = new A.Queue();

						instance.publish(
							{
								'evaluate': {
									defaultFn: instance._evaluate
								},
								start: {
									defaultFn: instance._start
								}
							}
						);

						instance.after('evaluationEnded', instance._afterEvaluationEnded);
					},

					evaluate: function(trigger, callback) {
						var instance = this;

						var enabled = instance.get('enabled');

						var form = instance.get('form');

						if (enabled && form) {
							if (instance.isEvaluating()) {
								instance.stop();
							}

							instance._evaluating = true;

							instance.fire(
								'start',
								{
									trigger: trigger
								}
							);

							instance._queue.add(trigger);

							instance.fire(
								'evaluate',
								{
									callback: function(result) {
										instance._evaluating = false;

										var triggers = {};

										while (instance._queue.size() > 0) {
											var next = instance._queue.next();

											if (!triggers[next.get('name')]) {
												instance.fire(
													'evaluationEnded',
													{
														result: result,
														trigger: next
													}
												);
											}

											triggers[next.get('name')] = true;
										}

										if (callback) {
											callback.apply(instance, arguments);
										}
									}
								}
							);
						}
					},

					isEvaluating: function() {
						var instance = this;

						return instance._evaluating;
					},

					stop: function() {
						var instance = this;

						if (instance._request) {
							instance._request.destroy();

							delete instance._request;
						}
					},

					_afterEvaluationEnded: function() {
						var instance = this;

						instance.stop();
					},

					_evaluate: function(event) {
						var instance = this;

						var callback = event.callback;

						instance._getRequestMessageType("DDMFormEvaluationRequest")
								.then(A.bind(instance._getRequestMessage, instance))
								.then(A.bind(instance._makeRequest, instance))
								.then(A.bind(instance._decodeResponse, instance))
								.then(A.bind(callback, instance))
								.catch(function(event) {
									if (event.details[1].statusText !== 'abort') {
										callback.call(instance, null);
									}
									else {
									 	callback.call(instance, {});
									}
								});
					},

					_getEnabled: function(enabled) {
						var instance = this;

						return enabled && !!instance.get('evaluatorURL');
					},

					_getRequestMessage: function(requestMessageType) {
						var instance = this;

						var form = instance.get('form');

						var payload = form.getEvaluationPayload();

						return new A.Promise(function (resolve) {
							var message = requestMessageType.fromObject(payload);

							resolve(requestMessageType.encode(message).finish());
						});
					},

					_decodeResponse: function(response) {
						var instance = this;

						return instance._getRequestMessageType("DDMFormEvaluationResponse")
								.then(function(responseMessageType) {
									var decodedResponse = responseMessageType.decode(new Uint8Array(response));

									return decodedResponse;
								});
					},

					_getRequestMessageType: function(type) {
						var instance = this;

						return new A.Promise(function(resolve, reject) {
							if (instance._rootMessageType) {
								resolve(instance._rootMessageType.lookupType(type));
							}
							else {
								Liferay.DDM.protobuf.load("/o/dynamic-data-mapping-form-renderer/protobuf/ddm-form-evaluation.proto", function(error, root) {
									if (error) {
										reject(error);
									}

									instance._rootMessageType = root;

									resolve(root.lookupType(type));
								});
							}
						})
					},

					_makeRequest: function(data) {
						var instance = this;

						return new A.Promise(function (resolve, reject) {
							var request = new A.IO.transports.xhr();

							request.open('POST', instance.get('evaluatorURL'));
							request.setRequestHeader('Content-Type', 'application/x-protobuf');
							request.responseType = "arraybuffer";

							request.onload = function() {
								if (request.aborted) {
									request.onerror();
									return;
								}

								resolve(request.response);
							};

							request.onerror = function() {
								var error = new Error('');

								error.request = request;

								reject(error)
							};

							request.send(data);
						});
					},

					_start: function(event) {
						var instance = this;

						if (instance.isEvaluating()) {
							instance.fire(
								'evaluationStarted',
								{
									trigger: event.trigger
								}
							);
						}
					},

					_valueEvaluatorURL: function() {
						var instance = this;

						var evaluatorURL;

						var form = instance.get('form');

						if (form) {
							evaluatorURL = form.get('evaluatorURL');
						}

						return evaluatorURL;
					}
				}
			}
		);

		Liferay.namespace('DDM.Renderer').ExpressionsEvaluator = ExpressionsEvaluator;
	},
	'',
	{
		requires: ['io-upload-iframe', 'aui-component', 'aui-promise', 'aui-io-request', 'liferay-ddm-protobufjs']
	}
);