using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Weapon : MonoBehaviour
{
    public int id;//���°�ΰ�
    public int prefabId;
    public float damage;
    public int count;//��� ��ġ�Ұų� -> ���� or ���� ��?
    public float speed;//ȸ�� �ӵ�, ����ü �ӵ� �� �������� �ӵ�
    public float coolTime;//�߻� ����, ȸ�� ���� ��

    float timer;
    Player player;

    void Awake()
    {
        player = GameManager.instance.player;
    }

    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {

            switch (id)
            {
               case 0:
                    transform.Rotate(Vector3.down * speed * Time.deltaTime);
                    break;
                case 1:
                    timer += Time.deltaTime;
                    if (timer > coolTime)
                    {
                        timer = 0;
                        Fire();
                    }
                        
                    break;
               default:
                    break;
            }
        
    }

    public void LevelUp(float damage, int count, float coolTime)
    {
        this.damage = damage;
        this.count += count;
        this.coolTime = coolTime;

        if (id == 0)
        {
            Batch();
        }

        transform.parent.BroadcastMessage("ApplyGear", SendMessageOptions.DontRequireReceiver);
    }

    public void Init(ItemData data)
    {
        name = "Weapon " + data.itemId;
        transform.parent = GameObject.Find("Support").transform;
        transform.localPosition = Vector3.zero;

        id = data.itemId;
        damage = data.baseDamage;
        count = data.baseCount;
        coolTime = data.baseCoolTime;

        for(int i = 0; i< GameManager.instance.pool.weaponPrefabs.Length; i++)
        {
            if(data.projectile == GameManager.instance.pool.weaponPrefabs[i])
            {
                prefabId = i;
            }
        }

        switch (id)
        {
            case 0:
                speed = 1200;
                Batch();
                StartCoroutine("ActiveWeapon");
                break;
            case 1:
                speed = 15;
                break;
            default:
                break;
        }

        transform.parent.BroadcastMessage("ApplyGear", SendMessageOptions.DontRequireReceiver);
    }

    void Batch()
    {
        for(int i=0; i<count; i++)
        {
            Transform hammer; 
                
            if(i < transform.childCount)
            {
                hammer = transform.GetChild(i);
            }
            else
            {
                hammer = GameManager.instance.pool.GetMelee(prefabId).transform;
                hammer.parent = transform;//�θ� �ڱ� �ڽ�
            }
            

            hammer.localPosition = Vector3.zero;
            hammer.localRotation = Quaternion.identity;


            Vector3 rotVec = Vector3.up * 360 * i / count;// + Vector3.right * 90;
            hammer.Rotate(rotVec);
            hammer.Translate(hammer.forward * 1.3f, Space.World);
            hammer.GetComponent<Hammer>().Init(damage, -1);//�������� -> ���Ѱ���
        }
    }

    void Fire()
    {
        if (!player.scanner.nearestTarget)
            return;

        Vector3 targetPos = player.scanner.nearestTarget.position;
        Vector3 dir = (targetPos - transform.position).normalized;

        Transform shoot = GameManager.instance.pool.Get(prefabId, false).transform;
        shoot.position = transform.position;
        shoot.rotation = Quaternion.FromToRotation(Vector3.right, dir);
        shoot.GetComponent<Hammer>().Init(damage, count, dir, speed);
    }

    //�ٵ� �̷� ���̸� Get �Լ����� ���� �� ������?
    IEnumerator ActiveWeapon()
    {
        //active true
        foreach (Transform child in transform)
            child.gameObject.SetActive(true);

        yield return new WaitForSeconds(0.3f);

        //active false
        foreach (Transform child in transform)
            child.gameObject.SetActive(false);
        yield return new WaitForSeconds(coolTime);//�� ä�� ��Ÿ�� ��ٸ���

        StartCoroutine("ActiveWeapon");
    }
}