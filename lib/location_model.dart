class LocationResponseModel {
  LocationResponseModel({
    required this.name,
    required this.lat,
    required this.lon,
    required this.country,
    required this.state,
  });

  final String? name;
  final double? lat;
  final double? lon;
  final String? country;
  final String? state;

  factory LocationResponseModel.fromJson(Map<String, dynamic> json) {
    return LocationResponseModel(
      name: json["name"],
      lat: json["lat"],
      lon: json["lon"],
      country: json["country"],
      state: json["state"],
    );
  }
}
