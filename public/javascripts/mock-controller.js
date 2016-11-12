/*global define */

'use strict';

require([ 'angular', './mock-dao' ], function() {

	var controllers = angular.module('myApp.mock-controller',
			[ 'myApp.mock-dao' ]);

	controllers.controller('mockCtrl', [ '$scope', 'mockDao','$q',
			function($scope, mockDao, $q) {

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
				
				function createMock(index) {
					mockDao.updateMock($scope.mockList[index]).then(listMocks);
				}
				
				function deleteMock(index) {
					var mockToDelete = $scope.mockList[index];
					if(!mockToDelete.create) {
						mockDao.deleteMock(mockToDelete).then(listMocks);
					}
					$scope.mockList.splice(index,1);
				}

				function showInfo(index) {
					if($scope.mockList[index].currentSide === "flippable_front") {
						$scope.mockList[index].currentSide = "flippable_back"
					} else {
						$scope.mockList[index].currentSide = "flippable_front"
					}
				}
				
				
			} ]);


	return controllers;

});