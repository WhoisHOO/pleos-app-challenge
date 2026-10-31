package com.whohoo.roadio.data

import com.whohoo.roadio.core.DocentStory
import com.whohoo.roadio.core.GeoPoint
import com.whohoo.roadio.core.RoutePath

object DemoRoute {

    val points: List<GeoPoint> = listOf(
        GeoPoint(37.57594, 126.97690),
        GeoPoint(37.57160, 126.97670),
        GeoPoint(37.56630, 126.97790),
        GeoPoint(37.56300, 126.97660),
        GeoPoint(37.55990, 126.97540),
        GeoPoint(37.55510, 126.97170),
        GeoPoint(37.55160, 126.98010),
        GeoPoint(37.55120, 126.98820),
    )

    val path: RoutePath = RoutePath(points)

    private fun at(fraction: Double): GeoPoint =
        path.pointAt(path.totalDistanceM * fraction)

    // Each story sits at its place (OpenStreetMap), Namsan on the climb; left, right and ahead follow the drive
    // south from Gwanghwamun.
    val stories: List<DocentStory> = listOf(
        DocentStory(
            id = "gwanghwamun",
            title = "광화문, 조선의 얼굴",
            body = "방금 출발한 곳이 광화문 앞입니다. 광화문은 조선 왕조의 법궁인 경복궁의 정문으로, 지금의 문은 2010년에 옛 모습대로 다시 세운 것입니다. 이제 세종대로를 따라 남쪽으로 내려가며 서울의 오래된 길을 지나갑니다.",
            point = GeoPoint(37.57604, 126.97702),
            triggerRadiusM = 300.0,
            priority = 2,
        ),
        DocentStory(
            id = "cheonggye",
            title = "청계천, 도심을 흐르는 물길",
            body = "곧 왼쪽으로 청계천이 시작되는 청계광장이 지나갑니다. 2005년 복원 이후 도심 한가운데를 흐르는 이 물길은 서울 사람들이 즐겨 찾는 산책길이 되었고, 여름에는 물놀이를 하는 아이들도 볼 수 있습니다.",
            point = GeoPoint(37.56921, 126.97798),
            triggerRadiusM = 320.0,
        ),
        DocentStory(
            id = "cityhall",
            title = "서울시청, 유리와 돌의 대화",
            body = "곧 왼쪽으로 돌로 지은 옛 서울시청이 보입니다. 1926년에 지어져 지금은 서울도서관으로 쓰이고, 그 뒤로 솟은 유리 건물이 2012년에 문을 연 새 서울시청입니다. 옛 건물의 돌벽과 새 건물의 유리벽이 나란히 서서 서울에서 가장 흥미로운 건축 대비를 보여 줍니다.",
            point = GeoPoint(37.56635, 126.97788),
            triggerRadiusM = 300.0,
        ),
        DocentStory(
            id = "namdaemun",
            title = "숭례문, 육백 년의 문",
            body = "곧 앞쪽으로 숭례문이 나타납니다. 남대문이라는 이름으로 더 익숙한 이 문은 조선 태조 때 세워졌고, 1962년 국보로 지정되어 오랫동안 '국보 1호'로 불렸습니다. 문 안쪽 남대문시장까지 걸어 들어가면 서울의 오래된 생활 냄새를 만날 수 있습니다.",
            point = GeoPoint(37.55999, 126.97536),
            triggerRadiusM = 320.0,
            priority = 2,
            placeName = "숭례문",
            address = "서울 중구 세종대로 40",
        ),
        DocentStory(
            id = "seoulstation",
            title = "서울역, 기차와 사람이 만나는 곳",
            body = "곧 오른쪽으로 붉은 벽돌 건물이 나타납니다. 1925년에 지어진 옛 서울역사로, 지금은 문화역서울 284라는 문화공간입니다. 바로 옆 새 역사에서는 KTX와 공항철도가 오가고, 지하철과 버스가 한데 모이는 서울의 관문입니다.",
            point = GeoPoint(37.55599, 126.97155),
            triggerRadiusM = 300.0,
            placeName = "문화역서울284",
            address = "서울 중구 통일로 1",
        ),
        DocentStory(
            id = "namsan",
            title = "남산, 도시 위의 숲",
            body = "이제 남산으로 올라갑니다. 차창 밖으로 나무가 가까워지고 공기가 달라지는 것을 느낄 수 있습니다. 남산은 높이가 300미터도 되지 않지만 서울 사람들에게는 가장 가까운 산이자, 도시를 한눈에 내려다보는 전망대입니다.",
            point = at(0.86),
            triggerRadiusM = 350.0,
        ),
        DocentStory(
            id = "nseoultower",
            title = "N서울타워, 도착",
            body = "앞쪽 산 위로 N서울타워가 보입니다. 남산 정상에 서 있는 이 타워에서는 서울의 사방이 한눈에 들어옵니다. 오늘은 여기서 이야기를 마칩니다. 안전하게 목적지에 도착하시길 바랍니다.",
            point = GeoPoint(37.55127, 126.98828),
            triggerRadiusM = 400.0,
            placeName = "N서울타워",
            address = "서울 용산구 남산공원길 105",
        ),
    )

    // What the car's navigation is given for this route: two stops on the way (it takes two waypoints at most), then
    // the end.
    val navigationStops: List<String> = listOf("namdaemun", "seoulstation", "nseoultower")
}
