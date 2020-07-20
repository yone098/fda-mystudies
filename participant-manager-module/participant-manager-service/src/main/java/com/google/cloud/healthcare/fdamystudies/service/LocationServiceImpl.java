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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    return LocationMapper.toLocationResponse(locationEntity, MessageCode.ADD_LOCATION_SUCCESS);
  }

  @Override
  @Transactional
  public LocationResponse updateLocation(UpdateLocationRequest locationRequest) {
    logger.entry("begin updateLocation()");

    Optional<LocationEntity> optLocation =
        locationRepository.findById(locationRequest.getLocationId());

    ErrorCode errorCode = validateUpdateLocationRequest(locationRequest, optLocation);

    if (errorCode != null) {
      logger.exit(errorCode);
      return new LocationResponse(errorCode);
    }

    LocationEntity locationEntity = optLocation.get();
    locationEntity.setName(
        StringUtils.defaultString(locationRequest.getName(), locationEntity.getName()));
    locationEntity.setDescription(
        StringUtils.defaultString(
            locationRequest.getDescription(), locationEntity.getDescription()));

    if (locationRequest.getStatus() != null) {
      locationEntity.setStatus(locationRequest.getStatus());
    }
    locationEntity = locationRepository.saveAndFlush(locationEntity);

    MessageCode messageCode = getMessageCodeByLocationStatus(locationRequest.getStatus());
    LocationResponse locationResponse =
        LocationMapper.toLocationResponse(locationEntity, messageCode);

    logger.exit(String.format("locationId=%s", locationEntity.getId()));
    return locationResponse;
  }

  private MessageCode getMessageCodeByLocationStatus(Integer status) {
    if (ACTIVE_STATUS.equals(status)) {
      return MessageCode.REACTIVE_SUCCESS;
    } else if (INACTIVE_STATUS.equals(status)) {
      return MessageCode.DECOMMISSION_SUCCESS;
    }
    return MessageCode.LOCATION_UPDATE_SUCCESS;
  }

  private ErrorCode validateUpdateLocationRequest(
      UpdateLocationRequest locationRequest, Optional<LocationEntity> optLocation) {
    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(locationRequest.getUserId());

    if (optUserRegAdminUser.isPresent()) {
      UserRegAdminEntity adminUser = optUserRegAdminUser.get();
      if (Permission.READ_EDIT != Permission.fromValue(adminUser.getManageLocations())) {
        return ErrorCode.LOCATION_UPDATE_DENIED;
      }
    }

    if (!optLocation.isPresent()) {
      return ErrorCode.LOCATION_NOT_FOUND;
    }

    LocationEntity locationEntity = optLocation.get();
    if (locationEntity.isDefault()) {
      return ErrorCode.DEFAULT_SITE_MODIFY_DENIED;
    }

    if (INACTIVE_STATUS.equals(locationRequest.getStatus())
        && INACTIVE_STATUS.equals(locationEntity.getStatus())) {
      return ErrorCode.ALREADY_DECOMMISSIONED;
    }
    List<SiteEntity> listOfSite =
        siteRepository.findByLocationIdAndStatus(locationRequest.getLocationId(), ACTIVE_STATUS);
    if (INACTIVE_STATUS.equals(locationRequest.getStatus())
        && CollectionUtils.isNotEmpty(listOfSite)) {
      return ErrorCode.CANNOT_DECOMMISSIONED;
    }

    if (ACTIVE_STATUS.equals(locationRequest.getStatus())
        && ACTIVE_STATUS.equals(locationEntity.getStatus())) {
      return ErrorCode.CANNOT_REACTIVE;
    }

    return null;
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
