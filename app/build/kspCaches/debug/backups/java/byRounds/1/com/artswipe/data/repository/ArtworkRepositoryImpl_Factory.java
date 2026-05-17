package com.artswipe.data.repository;

import com.artswipe.data.local.ArtworkDao;
import com.artswipe.data.remote.AicApi;
import com.artswipe.data.remote.MetApi;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class ArtworkRepositoryImpl_Factory implements Factory<ArtworkRepositoryImpl> {
  private final Provider<MetApi> metApiProvider;

  private final Provider<AicApi> aicApiProvider;

  private final Provider<ArtworkDao> artworkDaoProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  public ArtworkRepositoryImpl_Factory(Provider<MetApi> metApiProvider,
      Provider<AicApi> aicApiProvider, Provider<ArtworkDao> artworkDaoProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    this.metApiProvider = metApiProvider;
    this.aicApiProvider = aicApiProvider;
    this.artworkDaoProvider = artworkDaoProvider;
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public ArtworkRepositoryImpl get() {
    return newInstance(metApiProvider.get(), aicApiProvider.get(), artworkDaoProvider.get(), firestoreProvider.get());
  }

  public static ArtworkRepositoryImpl_Factory create(Provider<MetApi> metApiProvider,
      Provider<AicApi> aicApiProvider, Provider<ArtworkDao> artworkDaoProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    return new ArtworkRepositoryImpl_Factory(metApiProvider, aicApiProvider, artworkDaoProvider, firestoreProvider);
  }

  public static ArtworkRepositoryImpl newInstance(MetApi metApi, AicApi aicApi,
      ArtworkDao artworkDao, FirebaseFirestore firestore) {
    return new ArtworkRepositoryImpl(metApi, aicApi, artworkDao, firestore);
  }
}
