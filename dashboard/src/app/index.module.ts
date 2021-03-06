/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {Register} from '../components/utils/register';
import {FactoryConfig} from './factories/factories-config';
import {ComponentsConfig} from '../components/components-config';
import {AdminsConfig} from './admin/admin-config';
import {AdministrationConfig} from './administration/administration-config';
import {DiagnosticsConfig} from './diagnostics/diagnostics-config';
import {CheColorsConfig} from './colors/che-color.constant';
import {CheOutputColorsConfig} from './colors/che-output-colors.constant';
import {CheCountriesConfig} from './constants/che-countries.constant';
import {CheJobsConfig} from './constants/che-jobs.constant';
import {DashboardConfig} from './dashboard/dashboard-config';
// switch to a config
import {IdeConfig} from './ide/ide-config';
import {NavbarConfig} from './navbar/navbar-config';
import {ProjectsConfig} from './projects/projects-config';
import {ProxySettingsConfig} from './proxy/proxy-settings.constant';
import {WorkspacesConfig} from './workspaces/workspaces-config';
import {StacksConfig} from './stacks/stacks-config';
import {DemoComponentsController} from './demo-components/demo-components.controller';
import {CheBranding} from '../components/branding/che-branding.factory';
import {ChePreferences} from '../components/api/che-preferences.factory';
import {RoutingRedirect} from '../components/routing/routing-redirect.factory';
import IdeIFrameSvc from './ide/ide-iframe/ide-iframe.service';
import {CheIdeFetcher} from '../components/ide-fetcher/che-ide-fetcher.service';
import {RouteHistory} from '../components/routing/route-history.service';
import {CheUIElementsInjectorService} from '../components/service/injector/che-ui-elements-injector.service';
import {WorkspaceDetailsService} from './workspaces/workspace-details/workspace-details.service';

// init module
const initModule = angular.module('userDashboard', ['ngAnimate', 'ngCookies', 'ngTouch', 'ngSanitize', 'ngResource', 'ngRoute',
  'angular-websocket', 'ui.bootstrap', 'ui.codemirror', 'ngMaterial', 'ngMessages', 'angularMoment', 'angular.filter',
  'ngDropdowns', 'ngLodash', 'angularCharts', 'uuid4', 'angularFileUpload']);

// add a global resolve flag on all routes (user needs to be resolved first)
initModule.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
  $routeProvider.accessWhen = (path: string, route: che.route.IRoute) => {
    if (angular.isUndefined(route.resolve)) {
      route.resolve = {};
    }
    (route.resolve as any).app = ['cheBranding', '$q', 'chePreferences', (cheBranding: CheBranding, $q: ng.IQService, chePreferences: ChePreferences) => {
      const deferred = $q.defer();
      if (chePreferences.getPreferences()) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error: any) => {
          deferred.reject(error);
        });
      }
      return deferred.promise;
    }];

    return $routeProvider.when(path, route);
  };

  $routeProvider.accessOtherWise = (route: che.route.IRoute) => {
    if (angular.isUndefined(route.resolve)) {
      route.resolve = {};
    }
    (route.resolve as any).app = ['$q', 'chePreferences', ($q: ng.IQService, chePreferences: ChePreferences) => {
      const deferred = $q.defer();
      if (chePreferences.getPreferences()) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error: any) => {
          deferred.reject(error);
        });
      }
      return deferred.promise;
    }];
    return $routeProvider.otherwise(route);
  };


}]);

const DEV = false;

// configs
initModule.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
  // config routes (add demo page)
  if (DEV) {
    $routeProvider.accessWhen('/demo-components', {
      title: 'Demo Components',
      templateUrl: 'app/demo-components/demo-components.html',
      controller: 'DemoComponentsController',
      controllerAs: 'demoComponentsController',
      reloadOnSearch: false
    });
  }

  $routeProvider.accessOtherWise({
    redirectTo: '/workspaces'
  });
}]);

/**
 * Setup route redirect module
 */
initModule.run(['$rootScope', '$location', '$routeParams', 'routingRedirect', '$timeout', 'ideIFrameSvc', 'cheIdeFetcher', 'routeHistory', 'cheUIElementsInjectorService', 'workspaceDetailsService',
  ($rootScope: che.IRootScopeService, $location: ng.ILocationService, $routeParams: ng.route.IRouteParamsService, routingRedirect: RoutingRedirect, $timeout: ng.ITimeoutService, ideIFrameSvc: IdeIFrameSvc, cheIdeFetcher: CheIdeFetcher, routeHistory: RouteHistory, cheUIElementsInjectorService: CheUIElementsInjectorService, workspaceDetailsService: WorkspaceDetailsService) => {
    $rootScope.hideLoader = false;
    $rootScope.waitingLoaded = false;
    $rootScope.showIDE = false;

    workspaceDetailsService.addPage('SSH', '<workspace-details-ssh></workspace-details-ssh>', 'icon-ic_vpn_key_24px');

    // here only to create instances of these components
    /* tslint:disable */
    cheIdeFetcher;
    routeHistory;
    /* tslint:enable */

    $rootScope.$on('$viewContentLoaded', () => {
      cheUIElementsInjectorService.injectAll();
      $timeout(() => {
        if (!$rootScope.hideLoader) {
          if (!$rootScope.wantTokeepLoader) {
            $rootScope.hideLoader = true;
          } else {
            $rootScope.hideLoader = false;
          }
        }
        $rootScope.waitingLoaded = true;
      }, 1000);
    });

    $rootScope.$on('$routeChangeStart', (event: any, next: any) => {
      if (DEV) {
        console.log('$routeChangeStart event with route', next);
      }
    });

    $rootScope.$on('$routeChangeSuccess', (event: ng.IAngularEvent, next: ng.route.IRoute) => {
      const route = (<any>next).$$route;
      if (angular.isFunction(route.title)) {
        $rootScope.currentPage = route.title($routeParams);
      } else {
        $rootScope.currentPage = route.title || 'Dashboard';
      }
      const originalPath: string = route.originalPath;
      if (originalPath && originalPath.indexOf('/ide/') === -1) {
        $rootScope.showIDE = false;
      }
      // when a route is about to change, notify the routing redirect node
      if (next.resolve) {
        if (DEV) {
          console.log('$routeChangeSuccess event with route', next);
        }// check routes
        routingRedirect.check(event, next);
      }
    });

    $rootScope.$on('$routeChangeError', () => {
      $location.path('/');
    });
  }
]);

// add interceptors
initModule.factory('ETagInterceptor', ($window: ng.IWindowService, $cookies: ng.cookies.ICookiesService, $q: ng.IQService) => {

  const etagMap = {};

  return {
    request: (config: any) => {
      // add IfNoneMatch request on the che api if there is an existing eTag
      if ('GET' === config.method) {
        if (config.url.indexOf('/api') === 0) {
          const eTagURI = etagMap[config.url];
          if (eTagURI) {
            config.headers = config.headers || {};
            angular.extend(config.headers, {'If-None-Match': eTagURI});
          }
        }
      }
      return config || $q.when(config);
    },
    response: (response: any) => {

      // if response is ok, keep ETag
      if ('GET' === response.config.method) {
        if (response.status === 200) {
          const responseEtag = response.headers().etag;
          if (responseEtag) {
            if (response.config.url.indexOf('/api') === 0) {

              etagMap[response.config.url] = responseEtag;
            }
          }
        }

      }
      return response || $q.when(response);
    }
  };
});

initModule.config(($mdThemingProvider: ng.material.IThemingProvider, jsonColors: any) => {

  const cheColors = angular.fromJson(jsonColors);
  const getColor = (key: string) => {
    let color = cheColors[key];
    if (!color) {
      // return a flashy red color if color is undefined
      console.log('error, the color' + key + 'is undefined');
      return '#ff0000';
    }
    if (color.indexOf('$') === 0) {
      color = getColor(color);
    }
    return color;

  };

  const cheMap = $mdThemingProvider.extendPalette('indigo', {
    '500': getColor('$dark-menu-color'),
    '300': 'D0D0D0'
  });
  $mdThemingProvider.definePalette('che', cheMap);

  const cheDangerMap = $mdThemingProvider.extendPalette('red', {});
  $mdThemingProvider.definePalette('cheDanger', cheDangerMap);

  const cheWarningMap = $mdThemingProvider.extendPalette('orange', {
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheWarning', cheWarningMap);

  const cheGreenMap = $mdThemingProvider.extendPalette('green', {
    'A100': '#46AF00',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheGreen', cheGreenMap);

  const cheDefaultMap = $mdThemingProvider.extendPalette('blue', {
    'A400': getColor('$che-medium-blue-color')
  });
  $mdThemingProvider.definePalette('cheDefault', cheDefaultMap);

  const cheNoticeMap = $mdThemingProvider.extendPalette('blue', {
    'A400': getColor('$mouse-gray-color')
  });
  $mdThemingProvider.definePalette('cheNotice', cheNoticeMap);

  const cheAccentMap = $mdThemingProvider.extendPalette('blue', {
    '700': getColor('$che-medium-blue-color'),
    'A400': getColor('$che-medium-blue-color'),
    'A200': getColor('$che-medium-blue-color'),
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheAccent', cheAccentMap);

  const cheNavyPalette = $mdThemingProvider.extendPalette('purple', {
    '500': getColor('$che-navy-color'),
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheNavyPalette', cheNavyPalette);

  const toolbarPrimaryPalette = $mdThemingProvider.extendPalette('purple', {
    '500': getColor('$che-white-color'),
    'contrastDefaultColor': 'dark'
  });
  $mdThemingProvider.definePalette('toolbarPrimaryPalette', toolbarPrimaryPalette);

  const toolbarAccentPalette = $mdThemingProvider.extendPalette('purple', {
    'A200': 'EF6C00',
    '700': 'E65100',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('toolbarAccentPalette', toolbarAccentPalette);

  const cheGreyPalette = $mdThemingProvider.extendPalette('grey', {
    'A100': 'efefef',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheGrey', cheGreyPalette);

  $mdThemingProvider.theme('danger')
    .primaryPalette('che')
    .accentPalette('cheDanger')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('warning')
    .primaryPalette('che')
    .accentPalette('cheWarning')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('chesave')
    .primaryPalette('green')
    .accentPalette('cheGreen')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('checancel')
    .primaryPalette('che')
    .accentPalette('cheGrey')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('chedefault')
    .primaryPalette('che')
    .accentPalette('cheDefault')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('chenotice')
    .primaryPalette('che')
    .accentPalette('cheNotice')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('default')
    .primaryPalette('che')
    .accentPalette('cheAccent')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('toolbar-theme')
    .primaryPalette('toolbarPrimaryPalette')
    .accentPalette('toolbarAccentPalette');

  $mdThemingProvider.theme('factory-theme')
    .primaryPalette('light-blue')
    .accentPalette('pink')
    .warnPalette('red')
    .backgroundPalette('purple');

  $mdThemingProvider.theme('onboarding-theme')
    .primaryPalette('cheNavyPalette')
    .accentPalette('pink')
    .warnPalette('red')
    .backgroundPalette('purple');

  $mdThemingProvider.theme('dashboard-theme')
    .primaryPalette('cheNavyPalette')
    .accentPalette('pink')
    .warnPalette('red')
    .backgroundPalette('purple');

  $mdThemingProvider.theme('maincontent-theme')
    .primaryPalette('che')
    .accentPalette('cheAccent');
});

initModule.constant('userDashboardConfig', {
  developmentMode: DEV
});

initModule.config(['$routeProvider', '$httpProvider', ($routeProvider: che.route.IRouteProvider, $httpProvider: ng.IHttpProvider) => {
  // add the ETag interceptor for Che API
  $httpProvider.interceptors.push('ETagInterceptor');
}]);

const instanceRegister = new Register(initModule);

if (DEV) {
  instanceRegister.controller('DemoComponentsController', DemoComponentsController);
}

/* tslint:disable */
new ProxySettingsConfig(instanceRegister);
new CheColorsConfig(instanceRegister);
new CheOutputColorsConfig(instanceRegister);
new CheCountriesConfig(instanceRegister);
new CheJobsConfig(instanceRegister);
new ComponentsConfig(instanceRegister);
new AdminsConfig(instanceRegister);
new AdministrationConfig(instanceRegister);
new IdeConfig(instanceRegister);
new DiagnosticsConfig(instanceRegister);

new NavbarConfig(instanceRegister);
new ProjectsConfig(instanceRegister);
new WorkspacesConfig(instanceRegister);
new DashboardConfig(instanceRegister);
new StacksConfig(instanceRegister);
new FactoryConfig(instanceRegister);
/* tslint:enable */
