package com.artswipe.ui.screens.explanation;

import com.artswipe.data.local.ArtworkDao;
import com.artswipe.domain.repository.ArtworkRepository;
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
public final class ExplanationViewModel_Factory implements Factory<ExplanationViewModel> {
  private final Provider<ArtworkRepository> artworkRepositoryProvider;

  private final Provider<ArtworkDao> artworkDaoProvider;

  public ExplanationViewModel_Factory(Provider<ArtworkRepository> artworkRepositoryProvider,
      Provider<ArtworkDao> artworkDaoProvider) {
    this.artworkRepositoryProvider = artworkRepositoryProvider;
    this.artworkDaoProvider = artworkDaoProvider;
  }

  @Override
  public ExplanationViewModel get() {
    return newInstance(artworkRepositoryProvider.get(), artworkDaoProvider.get());
  }

  public static ExplanationViewModel_Factory create(
      Provider<ArtworkRepository> artworkRepositoryProvider,
      Provider<ArtworkDao> artworkDaoProvider) {
    return new ExplanationViewModel_Factory(artworkRepositoryProvider, artworkDaoProvider);
  }

  public static ExplanationViewModel newInstance(ArtworkRepository artworkRepository,
      ArtworkDao artworkDao) {
    return new ExplanationViewModel(artworkRepository, artworkDao);
  }
}
