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

    val stories: List<DocentStory> = listOf(
        DocentStory(
            id = "gwanghwamun",
            title = "광화문, 조선의 얼굴",
            body = "오른쪽으로 광화문이 보입니다. 조선 왕조 법궁인 경복궁의 정문으로, 지금의 문은 2010년에 원형대로 다시 세운 것입니다. 해질 무렵에는 문루 아래로 도심의 불빛이 겹쳐 아주 멋진 풍경이 됩니다.",
            point = at(0.01),
            triggerRadiusM = 300.0,
            priority = 2,
        ),
        DocentStory(
            id = "cheonggye",
            title = "청계천, 도심을 흐르는 물길",
            body = "잠시 왼쪽 아래로 시선을 내려 보세요. 청계천이 도심 한가운데를 가로질러 흐릅니다. 2005년 복원 이후 서울 사람들이 가장 즐겨 찾는 산책길이 되었고, 여름에는 물놀이를 하는 아이들도 볼 수 있습니다.",
            point = at(0.16),
            triggerRadiusM = 320.0,
        ),
        DocentStory(
            id = "cityhall",
            title = "서울시청, 유리와 돌의 대화",
            body = "왼쪽의 유리 건물이 새 서울시청입니다. 뒤편으로는 1926년에 지어진 옛 청사가 그대로 남아 있습니다. 옛 건물의 돌벽과 새 건물의 유리 커튼월이 나란히 서 있어 서울에서 가장 흥미로운 건축 대비를 보여 줍니다.",
            point = at(0.27),
            triggerRadiusM = 300.0,
        ),
        DocentStory(
            id = "namdaemun",
            title = "숭례문, 육백 년의 문",
            body = "곧 오른쪽으로 숭례문이 나타납니다. 남대문이라는 이름으로 더 익숙한 이 문은 조선 태조 때 세워졌고, 국보 제1호로 지정되어 있습니다. 문 안쪽 남대문시장까지 걸어 들어가면 서울의 오래된 생활 냄새를 만날 수 있습니다.",
            point = at(0.42),
            triggerRadiusM = 320.0,
            priority = 2,
        ),
        DocentStory(
            id = "seoulstation",
            title = "서울역, 기차와 사람이 만나는 곳",
            body = "왼쪽의 붉은 벽돌 건물은 1925년에 지어진 옛 서울역사입니다. 지금은 문화공간으로 쓰이고, 바로 옆 새 역사에서는 KTX와 공항철도가 오갑니다. 지하로 내려가면 지하철과 버스가 한꺼번에 이어지는 서울의 관문입니다.",
            point = at(0.55),
            triggerRadiusM = 300.0,
        ),
        DocentStory(
            id = "namsan",
            title = "남산, 도시 위의 숲",
            body = "이제 남산으로 올라갑니다. 차창 밖으로 나무가 가까워지고 공기가 달라지는 것을 느낄 수 있습니다. 남산은 높이가 262미터에 불과하지만 서울 사람들에게는 가장 가까운 산이자, 도시를 한눈에 내려다보는 전망대입니다.",
            point = at(0.86),
            triggerRadiusM = 350.0,
        ),
        DocentStory(
            id = "nseoultower",
            title = "N서울타워, 도착",
            body = "오른쪽 위로 N서울타워가 보입니다. 남산 정상에 서 있는 이 타워에서는 서울의 사방이 한눈에 들어옵니다. 오늘은 여기서 이야기를 마칩니다. 안전하게 목적지에 도착하시길 바랍니다.",
            point = at(0.98),
            triggerRadiusM = 400.0,
        ),
    )
}
