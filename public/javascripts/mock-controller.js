/*global define */

'use strict';

require([ 'angular', './mock-dao' ], function() {

  const numberOfPlotPoints = 1000

	var controllers = angular.module('myApp.mock-controller',
			[ 'myApp.mock-dao' ]);

	controllers.controller('mockCtrl', [ '$scope', 'mockDao','$q',
			function($scope, mockDao, $q) {
				var websocket = new WebSocket(getWsAddress());
				websocket.onmessage = updateStatistics

				$scope.newMock = newMock;
				$scope.createMock = createMock;
				$scope.updateMock = updateMock;
				$scope.deleteMock = deleteMock;
				$scope.mockList = [];
				$scope.showInfo = showInfo;

				activate();

				function activate() {
					listMocks();
				}

				function listMocks (){
					mockDao.getMocks().then(function(mocks) {
						var result = [];
						for(var i = 0; i < mocks.data.length; i++) {
							var mock = mocks.data[i];
							result.push({
								create: false,
								id: mock.id,
								type: mock.matchType,
								method: mock.matcher.resource.method,
								path: mock.matcher.resource.path,
								expectedBody: mock.matcher.body,
								body: mock.mockSpec.body,
								responseCode: mock.mockSpec.responseCode,
								contentType: mock.mockSpec.contentType,
								responseTimeMillis: mock.mockSpec.responseTimeMillis,
								currentSide: "flippable_front"
							})
						}
						$scope.mockList = result;
					})
				}


				function newMock(){
					$scope.mockUnderConstruction= true;
					$scope.mockList.push({
						create: true,
						method: "GET",
						path: "",
						body: "",
						type: "ignore",
						responseCode: "",
						contentType: "",
						responseTimeMillis: "",
						currentSide: "flippable_front"
					});
				};

				function addMock() {
					$scope.mockUnderConstruction = false;
				}

				function updateMock(index) {
					mockDao.updateMock($scope.mockList[index]).then(listMocks).then(addMock);
				}


				function createMock(index) {
					mockDao.createMock($scope.mockList[index]).then(listMocks).then(addMock);
				}

				function deleteMock(index) {
					var mockToDelete = $scope.mockList[index];
					if(!mockToDelete.create) {
						unWatchStatistics(mockToDelete);
						mockDao.deleteMock(mockToDelete).then(listMocks);
					} else {
						addMock();
					}
					$scope.mockList.splice(index,1);
					delete plotData["incoming" + mockToDelete.method + mockToDelete.path];
					delete plotData["completed" + mockToDelete.method + mockToDelete.path];
				}

				function getChartOptions () {
					return {
						series: {shadowSize: 0},
						  xaxis: {
							  show: false
						  }
					}
				}

				var plotData = {};

				function updatePlot(method, path, numberOfRequests, eventType) {

					var data = getHistoricData(eventType, method, path);
          const last = data[numberOfPlotPoints - 1];
          const lastIndex =  last[0];
          data.shift();
					data.push([lastIndex + 1, numberOfRequests]);

					var plot = $("#" + eventType + method + path).data("plot");
					if(plot) {
						plot.setData([data]);
						plot.setupGrid();
						plot.draw()
					}

				}

				function updateStatistics(msg) {
					var data = JSON.parse(msg.data);
					var method = data.resource.method;
					var path = data.resource.path;
					var eventType = data.eventType;
					var numberOfRequests = data.numberOfRequestsPerSecond;

					updatePlot(method, path, numberOfRequests,eventType);
				}




				function getHistoricData(eventType, method, path) {
					return plotData[eventType + method + path];
				}

				function createEmptyArray(length) {
				  var result = new Array(length);
          for (var i = 0; i < length; i++) {
            result[i] = [i, 0]
          }
          return result;
        }

				function watchStatistics(mock) {
					websocket.send(JSON.stringify({action:"watch", resource: {method: mock.method, path: mock.path}}));
					if(!plotData["incoming" + mock.method + mock.path]) {
						plotData["incoming" + mock.method + mock.path] = createEmptyArray(numberOfPlotPoints);
						plotData["completed" + mock.method + mock.path] = createEmptyArray(numberOfPlotPoints);
					}
					var incoming = getHistoricData("incoming", mock.method, mock.path);
					var completed = getHistoricData("completed", mock.method, mock.path);

					var incomingDataset = [
					               { label: "Incoming requests", data: incoming, points: { symbol: "triangle"} }
					           ];
					var completedDataset = [
							               { label: "Completed requests", data: completed, points: { symbol: "triangle"} }
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
					}
				}

				function getWsAddress() {
					var loc = window.location, new_uri;
					if (loc.protocol === "https:") {
					    new_uri = "wss:";
					} else {
					    new_uri = "ws:";
					}
					new_uri += "//" + loc.host;
					new_uri += loc.pathname + "ws";
					return new_uri;
				}


			} ]);


	return controllers;

});
