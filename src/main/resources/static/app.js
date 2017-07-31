(function () {
    var umaAws = angular.module('UmaAws', ['ngRoute', 'angularUtils.directives.dirPagination']);

    umaAws.directive('active', function ($location) {
        return {
            link: function (scope, element) {
                function makeActiveIfMatchesCurrentPath() {
                    if ($location.path().indexOf(element.find('a').attr('href').substr(1)) > -1) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                }

                scope.$on('$routeChangeSuccess', function () {
                    makeActiveIfMatchesCurrentPath();
                });
            }
        };
    });
    
    umaAws.directive('fileModel', [ '$parse', function($parse) {
    	return {
    		restrict : 'A',
    		link : function(scope, element, attrs) {
    			var model = $parse(attrs.fileModel);
    			var modelSetter = model.assign;

    			element.bind('change', function() {
    				scope.$apply(function() {
    					modelSetter(scope, element[0].files[0]);
    				});
    			});
    		}
    	};
    } ]);
    
    umaAws.controller('CreateUserCtrl', function ($scope, $location, $http) {
        var self = this;
        
        self.add = function () {            
        	var userModel = self.model;        	
        	var savedUser;
        	
        	var formData = new FormData();
        	formData.append('firstName', userModel.firstName);
        	formData.append('lastName', userModel.lastName);
        	formData.append('dateOfBirth', userModel.dateOfBirth.getFullYear()  + '-' +  (userModel.dateOfBirth.getMonth() + 1)  + '-' + userModel.dateOfBirth.getDay());
        	formData.append('image', userModel.image);
        	formData.append('street', userModel.address.street);
        	formData.append('town', userModel.address.town);
        	formData.append('county', userModel.address.county);
        	formData.append('postcode', userModel.address.postcode);
        		
        	$scope.saving=true;
        	$http.post('/uma/users', formData, {	
        	    transformRequest : angular.identity,
    			headers : {
    				'Content-Type' : undefined
    			}
    		}).success(function(savedUser) {
    			$scope.saving=false;
    			$location.path("/view-user/" + savedUser.id);
    		}).error(function(data) {
    			$scope.saving=false; 
    		});
        };
    });
    
    umaAws.controller('ViewUserCtrl', function ($scope, $http, $routeParams) {
        
    	var userId = $routeParams.userId;    	        
    	$scope.currentPage = 1;
    	$scope.pageSize = 10;
    	
    	$scope.dataLoading = true;
        $http.get('/uma/users/' + userId).then(function onSuccess(response) {
        	$scope.user = response.data;
        	$scope.dataLoading = false;
        }, function onError(response) {
        	$scope.customer = response.statusText;
        	$scope.dataLoading = false;
        });
    });
    
    umaAws.controller('ViewAllUsersCtrl', function ($scope, $http) {
    	
    	var self = this;
    	$scope.users = []; 
    	$scope.searchText;
        
        $scope.dataLoading = true;
        $http.get('/uma/users').then(function mySucces(response) {
        	$scope.users = response.data;
        	$scope.dataLoading = false;
        }, function myError(response) {
        	$scope.customer = response.statusText;
        	$scope.dataLoading = false;
        });        
        
        self.delete = function (userId) {
        	$scope.selectedUser = userId;
        	$scope.userDelete = true;
        	$http.delete('/uma/users/' + userId).then(function onSucces(response) {
            	$scope.users = _.without($scope.users, _.findWhere($scope.users, {id: userId}));
            	$scope.userDelete = false;
            }, function onError(){
            	
            });
        },
        
        $scope.searchFilter = function (obj) {
            var re = new RegExp($scope.searchText, 'i');
            return !$scope.searchText || re.test(obj.firstName) || re.test(obj.lastName.toString());
        };
    });
    
    umaAws.filter('formatDate', function() {
    	return function(input) {
    		return moment(input).format("DD-MM-YYYY");
    	};
    });
    
    umaAws.config(function ($routeProvider) {
        $routeProvider.when('/home', {templateUrl: 'pages/home.tpl.html'});
        $routeProvider.when('/create-user', {templateUrl: 'pages/createUser.tpl.html'});
        $routeProvider.when('/view-user/:userId', {templateUrl: 'pages/viewUser.tpl.html'});
        $routeProvider.when('/view-all-users', {templateUrl: 'pages/viewAllUsers.tpl.html'});
        $routeProvider.otherwise({redirectTo: '/home'});
    });
    
}());