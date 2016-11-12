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
		
		function updateMock(mock) {
			var responseCode = parseInt(mock.responseCode,10);
			var responseTimeMillis = parseInt(mock.responseTimeMillis,10)
			var updatedMock = {
				body:mock.body,
				responseCode: responseCode, 
				responseTimeMillis:	responseTimeMillis
			};
			console.log()
			return $http.put("mock/" + mock.method + "/" + mock.path, JSON.stringify(updatedMock));
		}
		
		function deleteMock(mock) {
			return $http.delete("mock/" + mock.method + "/" + mock.path);
		}
		
		return {
			getMocks : getMocks,
			getMock : getMock,
			getMockDetails : getMockDetails,
			updateMock:updateMock,
			deleteMock:deleteMock
		};
		/*
		,
			addTodo: function(todo){
				return $http.post("todos", todo);
			},
                        deleteTodo: function(todo){
                            return $http.delete("todos/" + todo.id);
                        }
                        */ 

		
	} ]);

});