using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Enemy : MonoBehaviour
{
    // ü�°� ������Ʈ�� ���� ���� ����
    public int maxHealth;
    public int curHealth;

    Rigidbody rigid;
    BoxCollider boxCollider;

    Material mat;
    public void TakeDamage(int damage)
    {
        curHealth -= damage;

        if(curHealth < 0)
        {
            Destroy(gameObject);
        }
    }

    void Awake()
    {
        rigid = GetComponent<Rigidbody>();
        boxCollider = GetComponent<BoxCollider>();
        mat = GetComponent<MeshRenderer>().material;
    }

    // �÷��̾ �ֵθ��� ��ġ Ȥ�� ���ƿ��� �Ѿ�
    // Ʈ���ŷ� ó��
    // OnTriggerEnter() �Լ��� �±� �� ������ �ۼ�
    void OnTriggerEnter(Collider other)
    {
        if(other.tag == "PlayerAttack")
        {
            Debug.Log("123123123123");
            //Player player = other.GetComponent<Player>();
            //Debug.Log(player.wSkillDamage);
            //curHealth -= player.wSkillDamage;
            curHealth -= 20;

            // ���� ��ġ�� �ǰ� ��ġ�� ���� ���ۿ� ���ϱ�
            Vector3 reactVec = transform.position - other.transform.position;

            // ���� ����� �� �����ǵ��� Destroy() ȣ��
            Destroy(other.gameObject);
            other.gameObject.SetActive(false);

            //StartCoroutine(OnDamage(reactVec, false));
        }

        /*if(other.tag == "Melee")
        {
            Weapon weapon = other.GetComponent<Weapon>();
            curHealth -= weapon.damage;
            // ���� ��ġ�� �ǰ� ��ġ�� ���� ���ۿ� ���ϱ�
            Vector3 reactVec = transform.position - other.transform.position;

            StartCoroutine(OnDamage(reactVec));

            //Debug.Log("Melee : " + curHealth);
        }
        else if(other.tag == "Bullet")
        {
            Bullet bullet = other.GetComponent<Bullet>();
            curHealth -= bullet.damage;
            // ���� ��ġ�� �ǰ� ��ġ�� ���� ���ۿ� ���ϱ�
            Vector3 reactVec = transform.position - other.transform.position;

            // �Ѿ��� ���, ���� ����� �� �����ǵ��� Destroy() ȣ��
            Destroy(other.gameObject);

            StartCoroutine(OnDamage(reactVec));
            //Debug.Log("Range : " + curHealth);
        }*/
    }
}