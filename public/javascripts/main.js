/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
    'angular': ['../lib/angularjs/angular'],
    'jquery': ['../lib/jquery/jquery.min'],
    'flot': ['../lib/flot/jquery.flot'],
	  'angular-ui': ['../lib/angular-ui-bootstrap/ui-bootstrap-tpls']
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
     },
	  'angular-ui' : {
		  deps: ['angular']
	  }
  }
});

require(['angular','flot','./mock-controller','angular-ui'],
  function(angular, flot,controllers) {

    // Declare app level module which depends on filters, and services

    var app = angular.module('myApp', [ 'myApp.mock-controller', 'ui.bootstrap']);

    var $html = angular.element(document.getElementsByTagName('html')[0]);

	angular.element().ready(function() {
		$html.addClass('ng-app');
		angular.bootstrap($html, [app['name']]);
	});

});
