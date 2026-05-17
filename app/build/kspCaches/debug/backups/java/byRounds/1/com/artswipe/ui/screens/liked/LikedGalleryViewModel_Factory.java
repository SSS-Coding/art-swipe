package com.artswipe.ui.screens.liked;

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
public final class LikedGalleryViewModel_Factory implements Factory<LikedGalleryViewModel> {
  private final Provider<ArtworkRepository> artworkRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public LikedGalleryViewModel_Factory(Provider<ArtworkRepository> artworkRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.artworkRepositoryProvider = artworkRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public LikedGalleryViewModel get() {
    return newInstance(artworkRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static LikedGalleryViewModel_Factory create(
      Provider<ArtworkRepository> artworkRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new LikedGalleryViewModel_Factory(artworkRepositoryProvider, authRepositoryProvider);
  }

  public static LikedGalleryViewModel newInstance(ArtworkRepository artworkRepository,
      AuthRepository authRepository) {
    return new LikedGalleryViewModel(artworkRepository, authRepository);
  }
}
