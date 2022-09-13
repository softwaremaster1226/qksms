package com.android.QKSMS.feature.fragment;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import com.android.QKSMS.R;

public class PermissionsFragmentDirections {
  private PermissionsFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionPermissionsToCamera() {
    return new ActionOnlyNavDirections(R.id.action_permissions_to_camera);
  }
}
