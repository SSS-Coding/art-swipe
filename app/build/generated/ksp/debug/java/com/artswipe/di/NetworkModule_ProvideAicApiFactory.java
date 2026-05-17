package com.artswipe.di;

import com.artswipe.data.remote.AicApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_ProvideAicApiFactory implements Factory<AicApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideAicApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public AicApi get() {
    return provideAicApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideAicApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideAicApiFactory(retrofitProvider);
  }

  public static AicApi provideAicApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideAicApi(retrofit));
  }
}
