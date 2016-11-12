/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
    'angular': ['../lib/angularjs/angular'],
    'jquery': ['../lib/jquery/jquery.min'],
    'flot': ['../lib/flot/jquery.flot']
  },
  shim: {
	'angular': {
		exports : 'angular',
		
    },
    'jquery': {
        exports : 'jquery',
        deps: ['angular']
     },
     'flot': {
         deps: ['jquery']
     }
  }
});

require(['angular','flot','./mock-controller'],
  function(angular, flot,controllers) {

    // Declare app level module which depends on filters, and services

    var app = angular.module('myApp', [ 'myApp.mock-controller']);
    
    var $html = angular.element(document.getElementsByTagName('html')[0]);

	angular.element().ready(function() {
		$html.addClass('ng-app');
		angular.bootstrap($html, [app['name']]);
	});    

});