package com.artswipe.di;

import com.artswipe.data.remote.MetApi;
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
public final class NetworkModule_ProvideMetApiFactory implements Factory<MetApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideMetApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public MetApi get() {
    return provideMetApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideMetApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideMetApiFactory(retrofitProvider);
  }

  public static MetApi provideMetApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideMetApi(retrofit));
  }
}
