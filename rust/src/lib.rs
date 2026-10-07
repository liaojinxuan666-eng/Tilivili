use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;

// 64位字符重排表，B站固定算法
const MIXIN_KEY_ENC_TAB: [usize; 64] = [
    46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
    27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
    37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4,
    22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52,
];

#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_helloRust(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let output = env.new_string("Hello from Rust!").expect("Couldn't create java string!");
    output.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_wbiSign(
    mut env: JNIEnv,
    _class: JClass,
    img_key: JString,
    sub_key: JString,
    raw_query: JString,
    wts: JString, // 传入时间戳，保证签名时效性
) -> jstring {
    let img_key: String = env.get_string(&img_key).expect("Couldn't get img_key").into();
    let sub_key: String = env.get_string(&sub_key).expect("Couldn't get sub_key").into();
    let raw_query: String = env.get_string(&raw_query).expect("Couldn't get raw_query").into();
    let wts: String = env.get_string(&wts).expect("Couldn't get wts").into();

    // 1. 计算 mixin_key
    let orig = format!("{}{}", img_key, sub_key);
    let mut mixin_key = String::new();
    for &idx in MIXIN_KEY_ENC_TAB.iter() {
        if idx < orig.len() {
            mixin_key.push(orig.chars().nth(idx).unwrap());
        }
    }
    
    // 2. 拼接参数和 mixin_key
    // 这里为了简化，实际开发中还需要对 raw_query 进行 URL 编码和排序
    let mixed = format!("{}{}", raw_query, mixin_key);
    
    // 3. 加上 wts 再算 MD5
    let sign_str = format!("{}{}", mixed, wts);
    let digest = md5::compute(sign_str.as_bytes());
    let sign = format!("{:x}", digest);

    let output = env.new_string(sign).expect("Couldn't create java string!");
    output.into_raw()
}