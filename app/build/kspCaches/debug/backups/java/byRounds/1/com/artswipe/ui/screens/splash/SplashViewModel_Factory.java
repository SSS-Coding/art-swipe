package com.artswipe.ui.screens.splash;

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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<ArtworkRepository> artworkRepositoryProvider;

  public SplashViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<ArtworkRepository> artworkRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.artworkRepositoryProvider = artworkRepositoryProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(authRepositoryProvider.get(), artworkRepositoryProvider.get());
  }

  public static SplashViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<ArtworkRepository> artworkRepositoryProvider) {
    return new SplashViewModel_Factory(authRepositoryProvider, artworkRepositoryProvider);
  }

  public static SplashViewModel newInstance(AuthRepository authRepository,
      ArtworkRepository artworkRepository) {
    return new SplashViewModel(authRepository, artworkRepository);
  }
}
