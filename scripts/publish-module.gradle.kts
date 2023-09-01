import com.skydoves.retrofit.adapters.Configuration

apply(plugin = "com.vanniktech.maven.publish")

rootProject.extra.apply {
  val snapshot = System.getenv("SNAPSHOT").toBoolean()
  val libVersion = if (snapshot) {
    Configuration.snapshotVersionName
  } else {
    Configuration.versionName
  }
  set("libVersion", libVersion)
}
