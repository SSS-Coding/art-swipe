package com.artswipe.di;

import com.artswipe.data.local.ArtDatabase;
import com.artswipe.data.local.ArtworkDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideArtworkDaoFactory implements Factory<ArtworkDao> {
  private final Provider<ArtDatabase> databaseProvider;

  public DatabaseModule_ProvideArtworkDaoFactory(Provider<ArtDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ArtworkDao get() {
    return provideArtworkDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideArtworkDaoFactory create(
      Provider<ArtDatabase> databaseProvider) {
    return new DatabaseModule_ProvideArtworkDaoFactory(databaseProvider);
  }

  public static ArtworkDao provideArtworkDao(ArtDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideArtworkDao(database));
  }
}
