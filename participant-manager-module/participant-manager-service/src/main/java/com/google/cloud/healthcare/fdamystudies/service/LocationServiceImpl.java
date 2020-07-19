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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import com.google.cloud.healthcare.fdamystudies.repository.StudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;

@Service
public class LocationServiceImpl implements LocationService {

  private XLogger logger = XLoggerFactory.getXLogger(LocationServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private LocationRepository locationRepository;

  @Autowired private SiteRepository siteRepository;

  @Autowired private StudyRepository studyRepository;

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

  @Override
  @Transactional
  public LocationResponse getLocations(String userId, String locationId) {
    logger.entry("begin getLocations()");

    Optional<UserRegAdminEntity> optUserRegAdminUser = userRegAdminRepository.findById(userId);

    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    // TODO (Madhurya) null value check
    if (adminUser.getManageLocations() == Permission.NO_PERMISSION.value()) {
      logger.exit(
          String.format(
              "Get locations failed with error code=%s", ErrorCode.LOCATION_ACCESS_DENIED));
      return new LocationResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }
    return getLocationsByLocationId(locationId);
  }

  public LocationResponse getLocationsByLocationId(String locationId) {
    List<LocationEntity> listOfEntity;
    // TODO Madhurya if condition used in predicates so implemented like this, is there any other
    // way??
    if (!StringUtils.isEmpty(locationId)) {
      listOfEntity = locationRepository.getListOfLocationId(locationId);
    } else {
      listOfEntity = locationRepository.findAll();
    }

    if (listOfEntity.isEmpty()) {
      logger.exit(
          String.format("Get locations failed with error code=%s", ErrorCode.LOCATION_NOT_FOUND));
      return new LocationResponse(ErrorCode.LOCATION_NOT_FOUND);
    }
    List<LocationRequest> listOfLocationRequest = new LinkedList<>();
    List<String> locationIds = new LinkedList<>();

    for (LocationEntity locationEntity : listOfEntity) {
      LocationRequest locationRequest = new LocationRequest();
      locationRequest.setLocationId(locationEntity.getId());
      locationRequest.setName(locationEntity.getName());
      locationRequest.setDescription(locationEntity.getDescription());
      locationRequest.setCustomId(locationEntity.getCustomId());
      locationRequest.setStatus(locationEntity.getStatus());
      listOfLocationRequest.add(locationRequest);
      locationIds.add(locationEntity.getId());
    }

    Map<String, List<String>> locationStudies = getStudiesForLocations(locationIds);
    if (locationStudies != null) {
      for (LocationRequest locReq : listOfLocationRequest) {
        List<String> studies = locationStudies.get(locReq.getLocationId());
        if (studies != null) {
          locReq.setStudies(studies);
          locReq.setStudiesCount(studies.size());
        }
      }
    }
    LocationResponse locationResponse = new LocationResponse(MessageCode.GET_LOCATION_SUCCESS);
    locationResponse.setLocations(listOfLocationRequest);
    return locationResponse;
  }

  public Map<String, List<String>> getStudiesForLocations(List<String> locationIds) {

    List<Object[]> rows = studyRepository.getStudiesForLocations(locationIds);
    Map<String, List<String>> locationStudies = new HashMap<>();
    if (!CollectionUtils.isEmpty(rows)) {
      for (Object[] row : rows) {
        String locationId = (String) row[0];
        String studiesString = (String) row[1];
        if (!StringUtils.isBlank(studiesString)) {
          List<String> studies = Arrays.asList(studiesString.split(","));
          locationStudies.put(locationId, studies);
        }
      }
    }
    return locationStudies;
  }

  @Override
  public LocationResponse getLocationsForSite(String userId, String studyId) {
    Optional<UserRegAdminEntity> optUserRegAdminUser = userRegAdminRepository.findById(userId);

    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    // TODO (Madhurya)for null check
    if (adminUser.getManageLocations() == Permission.NO_PERMISSION.value()) {
      logger.exit(
          String.format(
              "Get locations for site failed with error code=%s",
              ErrorCode.LOCATION_ACCESS_DENIED));
      return new LocationResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }
    List<LocationEntity> listOfLocation = locationRepository.getLocationsForSite(studyId);

    LocationResponse locationResponse =
        new LocationResponse(MessageCode.GET_LOCATION_FOR_SITE_SUCCESS);
    locationResponse.setLocations(LocationMapper.listOfLocationRequest(listOfLocation));

    return locationResponse;
  }
}
