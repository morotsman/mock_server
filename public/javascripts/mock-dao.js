/*global define */

'use strict';

define([ 'angular' ], function(angular) {

	var myModule = angular.module('myApp.mock-dao', []);

	myModule.factory('mockDao', [ '$http','$q', function($http,$q) {

		function getMocks() {
			return $http.get("mock");
		}

		function getMock(method,path) {
			return $http.get("mock/" + method + "/" + path);
		}

		function getMockDetails(mocks) {
			return $q.all(mocks.map(function(m){
				return getMock(m.method,m.path);
			}));
		}

		function createMock(mock) {
			var responseCode = parseInt(mock.responseCode,10);
			var responseTimeMillis = parseInt(mock.responseTimeMillis,10)
			return $http.post("mock", JSON.stringify(toMock(mock, responseCode, responseTimeMillis)));
		}

		function toMock(mock, responseCode, responseTimeMillis) {
			return {
				mockResource: {
					method: mock.method,
					path: mock.path,
					body: mock.expectedBody
				},
				mockSpec: {
					body:mock.body,
					responseCode: responseCode,
					responseTimeMillis:	responseTimeMillis,
					contentType: mock.contentType
				}
			};
		}

		function updateMock(mock) {
			var responseCode = parseInt(mock.responseCode,10);
			var responseTimeMillis = parseInt(mock.responseTimeMillis,10)
			return $http.put("mock/" + mock.id, JSON.stringify(toMock(mock, responseCode, responseTimeMillis)));
		}

		function deleteMock(mock) {
			return $http.delete("mock/" + mock.id);
		}

		return {
			getMocks : getMocks,
			getMock : getMock,
			getMockDetails : getMockDetails,
			createMock:createMock,
			updateMock:updateMock,
			deleteMock:deleteMock
		};
	} ]);

});
