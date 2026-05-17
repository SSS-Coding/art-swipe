package com.artswipe.ui.screens.compatibility;

import com.artswipe.domain.repository.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class CompatibilityViewModel_Factory implements Factory<CompatibilityViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public CompatibilityViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public CompatibilityViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static CompatibilityViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new CompatibilityViewModel_Factory(authRepositoryProvider);
  }

  public static CompatibilityViewModel newInstance(AuthRepository authRepository) {
    return new CompatibilityViewModel(authRepository);
  }
}
