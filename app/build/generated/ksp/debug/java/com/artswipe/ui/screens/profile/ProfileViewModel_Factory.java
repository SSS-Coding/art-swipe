package com.artswipe.ui.screens.profile;

import com.artswipe.domain.repository.ArtworkRepository;
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<ArtworkRepository> artworkRepositoryProvider;

  public ProfileViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<ArtworkRepository> artworkRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.artworkRepositoryProvider = artworkRepositoryProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(authRepositoryProvider.get(), artworkRepositoryProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<ArtworkRepository> artworkRepositoryProvider) {
    return new ProfileViewModel_Factory(authRepositoryProvider, artworkRepositoryProvider);
  }

  public static ProfileViewModel newInstance(AuthRepository authRepository,
      ArtworkRepository artworkRepository) {
    return new ProfileViewModel(authRepository, artworkRepository);
  }
}
