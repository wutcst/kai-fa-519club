package cn.edu.whut.sept.zuul.level;

/**
 * F6 联机计时权威：单机由本地实时秒表扣减，联机由服务端同步剩余秒数。
 */
public enum TimerAuthority {

    /** 单机 / 命令行：本地每秒自动流逝 */
    LOCAL_CLIENT,

    /** 联机客户端：不本地 tick，仅接受服务端推送的剩余秒数 */
    SERVER_CLIENT,

    /** 联机服务端：持有权威倒计时并广播 */
    SERVER_HOST
}
