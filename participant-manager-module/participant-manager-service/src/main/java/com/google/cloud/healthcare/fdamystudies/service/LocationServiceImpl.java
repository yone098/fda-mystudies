/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.INACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.YES;

import java.util.List;
import java.util.Optional;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.cloud.healthcare.fdamystudies.beans.LocationRequest;
import com.google.cloud.healthcare.fdamystudies.beans.LocationResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateLocationRequest;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.mapper.LocationMapper;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.LocationRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;

@Service
public class LocationServiceImpl implements LocationService {

  private XLogger logger = XLoggerFactory.getXLogger(LocationServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private LocationRepository locationRepository;

  @Autowired private SiteRepository siteRepository;

  @Override
  @Transactional
  public LocationResponse addNewLocation(LocationRequest locationRequest) {
    logger.entry("begin addNewLocation()");

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(locationRequest.getUserId());

    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (!adminUser.getManageLocations().equals(Permission.READ_EDIT.value())) {
      logger.exit(
          String.format(
              "Add location failed with error code=%s", ErrorCode.LOCATION_ACCESS_DENIED));
      return new LocationResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }
    LocationEntity locationEntity = LocationMapper.fromLocationRequest(locationRequest);
    locationEntity.setCreatedBy(adminUser.getId());
    locationEntity = locationRepository.saveAndFlush(locationEntity);
    logger.exit(String.format("locationId=%s", locationEntity.getId()));

    return LocationMapper.toLocationResponse(
        locationEntity, new LocationResponse(MessageCode.ADD_LOCATION_SUCCESS));
  }

  @Override
  @Transactional
  public LocationResponse updateLocation(UpdateLocationRequest locationRequest) {
    logger.entry("begin updateLocation()");

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(locationRequest.getUserId());
    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (!adminUser.getManageLocations().equals(Permission.READ_EDIT.value())) {
      logger.exit(
          String.format(
              "Update location failed with error code=%s", ErrorCode.LOCATION_ACCESS_DENIED));
      return new LocationResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }

    Optional<LocationEntity> optLocation =
        locationRepository.findById(locationRequest.getLocationId());
    if (!optLocation.isPresent()) {
      logger.exit(
          String.format("Update location failed with error code=%s", ErrorCode.LOCATION_NOT_FOUND));
      return new LocationResponse(ErrorCode.LOCATION_NOT_FOUND);
    }
    LocationEntity locationEntity = optLocation.get();

    if (locationEntity.getIsDefault().equals(YES)) {
      logger.exit(
          String.format(
              "Update location failed with error code=%s", ErrorCode.DEFAULT_SITE_MODIFY_DENIED));
      return new LocationResponse(ErrorCode.DEFAULT_SITE_MODIFY_DENIED);
    }
    locationEntity.setCustomId(locationEntity.getCustomId());
    locationEntity.setName(
        locationRequest.getName() == null ? locationEntity.getName() : locationRequest.getName());
    locationEntity.setDescription(
        locationRequest.getDescription() == null
            ? locationEntity.getDescription()
            : locationRequest.getDescription());

    // TODO(Madhurya) status changed to Integer from String,..should inform IOS team??
    if (locationRequest.getStatus() == null) {
      locationEntity = locationRepository.saveAndFlush(locationEntity);

      // TODO(Madhurya) Previous code is not having message for this case,it was null??I have set it
      // Do i need to remove??
      LocationResponse locationResponse =
          LocationMapper.toLocationResponse(
              locationEntity, new LocationResponse(MessageCode.LOCATION_UPDATE_SUCCESS));
      locationResponse.setHttpStatusCode(200);
      logger.exit(String.format("locationId=%s", locationEntity.getId()));
      return locationResponse;
    } else if (locationRequest.getStatus() == INACTIVE_STATUS) {
      if (locationEntity.getStatus() == INACTIVE_STATUS) {
        logger.exit(
            String.format(
                "Update location failed with error code=%s", ErrorCode.ALREADY_DECOMMISSIONED));
        return new LocationResponse(ErrorCode.ALREADY_DECOMMISSIONED);
      }
      List<SiteEntity> listOfSite =
          siteRepository.findByLocationIdAndStatus(locationRequest.getLocationId(), ACTIVE_STATUS);
      if (!CollectionUtils.isEmpty(listOfSite)) {
        logger.exit(
            String.format(
                "Update location failed with error code=%s", ErrorCode.CANNOT_DECOMMISSIONED));
        return new LocationResponse(ErrorCode.CANNOT_DECOMMISSIONED);
      }

      locationEntity.setStatus(INACTIVE_STATUS);
      locationEntity = locationRepository.saveAndFlush(locationEntity);

      logger.exit(String.format("locationId=%s", locationEntity.getId()));
      return LocationMapper.toLocationResponse(
          locationEntity, new LocationResponse(MessageCode.DECOMMISSION_SUCCESS));
    } else {
      if (locationEntity.getStatus() == ACTIVE_STATUS) {
        logger.exit(
            String.format("Update location failed with error code=%s", ErrorCode.CANNOT_REACTIVE));
        return new LocationResponse(ErrorCode.CANNOT_REACTIVE);
      }
      locationEntity.setStatus(ACTIVE_STATUS);
      locationEntity = locationRepository.saveAndFlush(locationEntity);

      logger.exit(String.format("locationId=%s", locationEntity.getId()));
      return LocationMapper.toLocationResponse(
          locationEntity, new LocationResponse(MessageCode.REACTIVE_SUCCESS));
    }
  }
}
