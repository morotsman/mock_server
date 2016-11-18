/*global define */

'use strict';

require([ 'angular', './mock-dao' ], function() {

	var controllers = angular.module('myApp.mock-controller',
			[ 'myApp.mock-dao' ]);

	controllers.controller('mockCtrl', [ '$scope', 'mockDao','$q',
			function($scope, mockDao, $q) {
				var websocket = new WebSocket("ws://localhost:9000/ws");
				websocket.onmessage = updateStatistics

				$scope.newMock = newMock; 
				$scope.createMock = createMock;
				$scope.deleteMock = deleteMock;
				$scope.mockList = [];
				$scope.showInfo = showInfo;
		
				activate();
				
				function activate() {
					listMocks();
				}
 
				function listMocks (){
					mockDao.getMocks().then(function(mocks) {
						mockDao.getMockDetails(mocks.data).then(function(mockDetails){
							var result = [];
							for(var i = 0; i < mocks.data.length; i++) {
								result.push({
									create: false,
									method: mocks.data[i].method,
									path: mocks.data[i].path,
									body: mockDetails[i].data.body,
									responseCode: mockDetails[i].data.responseCode,
									responseTimeMillis: mockDetails[i].data.responseTimeMillis,
									currentSide: "flippable_front"
								})
							}
							$scope.mockList = result;
						});
					})
				}
				
				
				function newMock(){
					$scope.mockUnderConstruction= true;
					$scope.mockList.push({
						create: true,
						method: "",
						path: "",
						body: "",
						responseCode: "",
						responseTimeMillis: "",
						currentSide: "flippable_front"
					});
				};
				
				function addMock() {
					$scope.mockUnderConstruction = false;
				}
				
				function createMock(index) {
					mockDao.updateMock($scope.mockList[index]).then(listMocks).then(addMock);
				}
				
				function deleteMock(index) {
					var mockToDelete = $scope.mockList[index];
					if(!mockToDelete.create) {
						mockDao.deleteMock(mockToDelete).then(listMocks);
					}
					$scope.mockList.splice(index,1);
				}

				function getChartOptions () {
					return {
						series: {shadowSize: 0},
						  xaxis: {
							  show: false
						  }				    
					}
				}	
				

				
				function updatePlot(method, path, numberOfRequests, eventType) {
					var plot = $("#" + eventType + method + path).data("plot")
					var data = plot.getData()[0].data;
					if(data.length > 1000) {
						data.shift();
					}
					data.push([data.length, numberOfRequests]);
					
					plot.setData([data])
					plot.setupGrid()
					plot.draw()
				}
				
				function updateStatistics(msg) {
					var data = JSON.parse(msg.data);
					var method = data.resource.method;
					var path = data.resource.path;
					var eventType = data.eventType
					var numberOfRequests = data.numberOfRequestsPerSecond;
					
					updatePlot(method, path, numberOfRequests,eventType);
				}
				
				
				function watchStatistics(mock) {
					websocket.send(JSON.stringify({action:"watch", resource: {method: mock.method, path: mock.path}}));
					var incomingDataset = [
					               { label: "Outgoing requests", data: [], points: { symbol: "triangle"} }
					           ];
					var completedDataset = [
							               { label: "Outgoing requests", data: [], points: { symbol: "triangle"} }
							           ];
					var chartOptions = getChartOptions();
					$('#completed' + mock.method + mock.path).plot(completedDataset, chartOptions).data("plot");
					$('#incoming' + mock.method + mock.path).plot(incomingDataset, chartOptions).data("plot")
				}
				
				function unWatchStatistics(mock) {
					websocket.send(JSON.stringify({action:"unWatch", resource: {method: mock.method, path: mock.path}}));
				}

				function showInfo(index) {
					var mock = $scope.mockList[index]; 
					if(mock.currentSide === "flippable_front") {
						mock.currentSide = "flippable_back";
						watchStatistics(mock);
					} else {
						mock.currentSide = "flippable_front";
						unWatchStatistics(mock);
					}
				}
				
				
			} ]);


	return controllers;

});