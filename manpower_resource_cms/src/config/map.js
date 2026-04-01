/**
 * 百度地图配置
 * 请在百度地图开放平台申请 AK: https://lbsyun.baidu.com/apiconsole/key
 */
export const BMAP_CONFIG = {
  // 百度地图 JS API AK
  ak: '8ZS6K82VlDPQpvFfCqEMu9gFvc8exre4',
}

/**
 * 动态加载百度地图 JS API
 */
export function loadBMapScript() {
  return new Promise((resolve, reject) => {
    // 如果已经加载过，直接返回
    if (typeof BMap !== 'undefined') {
      resolve(BMap)
      return
    }

    // 定义回调函数
    window.initBMap = () => {
      resolve(BMap)
      delete window.initBMap
    }

    // 创建 script 标签
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = `https://api.map.baidu.com/api?v=3.0&ak=${BMAP_CONFIG.ak}&callback=initBMap`

    script.onerror = () => {
      reject(new Error('百度地图 API 加载失败'))
    }

    document.head.appendChild(script)
  })
}
